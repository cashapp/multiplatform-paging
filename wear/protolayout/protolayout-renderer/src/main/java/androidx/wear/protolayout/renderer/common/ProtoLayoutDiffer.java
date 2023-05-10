/*
 * Copyright 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.wear.protolayout.renderer.common;

import static androidx.core.util.Preconditions.checkState;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.annotation.RestrictTo.Scope;
import androidx.annotation.VisibleForTesting;
import androidx.wear.protolayout.proto.FingerprintProto.NodeFingerprint;
import androidx.wear.protolayout.proto.FingerprintProto.TreeFingerprint;
import androidx.wear.protolayout.proto.LayoutElementProto.ArcLayoutElement;
import androidx.wear.protolayout.proto.LayoutElementProto.Layout;
import androidx.wear.protolayout.proto.LayoutElementProto.LayoutElement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Utility to diff 2 proto layouts in order to be able to partially update the display. */
@RestrictTo(Scope.LIBRARY_GROUP)
public class ProtoLayoutDiffer {
    /** Prefix for all node IDs generated by this differ. */
    @NonNull private static final String NODE_ID_PREFIX = "pT";

    /** Node ID of the root node. */
    @RestrictTo(Scope.LIBRARY_GROUP)
    @NonNull
    public static final String ROOT_NODE_ID = NODE_ID_PREFIX + "1";

    // This must match {@code Fingerprint.DISCARDED_VALUE}
    @VisibleForTesting static final int DISCARDED_FINGERPRINT_VALUE = -1;

    /**
     * If true, an element addition or removal forces its parent (and siblings of the changed node)
     * to reinflate.
     */
    @RestrictTo(Scope.LIBRARY_GROUP)
    public static final boolean UPDATE_ALL_CHILDREN_AFTER_ADD_REMOVE = true;

    /**
     * Index of the first child node under a parent. {@link #createNodePosId} should be called
     * starting from this value and incremented by one for each child node.
     */
    @RestrictTo(Scope.LIBRARY_GROUP)
    public static final int FIRST_CHILD_INDEX = 0;

    private enum NodeChangeType {
        NO_CHANGE,
        CHANGE_IN_SELF_ONLY,
        CHANGE_IN_SELF_AND_ALL_CHILDREN,
        CHANGE_IN_SELF_AND_SOME_CHILDREN,
        CHANGE_IN_CHILDREN
    }

    static final class InconsistentFingerprintException extends Exception {}

    /** A node in a layout tree. */
    private static final class TreeNode {
        @Nullable final LayoutElement mLayoutElement;
        @Nullable final ArcLayoutElement mArcLayoutElement;
        @NonNull final NodeFingerprint mFingerprint;
        @NonNull final String mPosId;

        private TreeNode(
                @Nullable LayoutElement layoutElement,
                @Nullable ArcLayoutElement arcLayoutElement,
                @NonNull NodeFingerprint fingerprint,
                @NonNull String posId) {
            this.mLayoutElement = layoutElement;
            this.mArcLayoutElement = arcLayoutElement;
            this.mFingerprint = fingerprint;
            this.mPosId = posId;
        }

        @NonNull
        static TreeNode ofLayoutElement(
                @NonNull LayoutElement layoutElement,
                @NonNull NodeFingerprint fingerprint,
                @NonNull String posId) {
            return new TreeNode(layoutElement, null, fingerprint, posId);
        }

        @NonNull
        static TreeNode ofArcLayoutElement(
                @NonNull ArcLayoutElement arcLayoutElement,
                @NonNull NodeFingerprint fingerprint,
                @NonNull String id) {
            return new TreeNode(null, arcLayoutElement, fingerprint, id);
        }

        @NonNull
        TreeNodeWithChange withChange(boolean isSelfOnlyChange) {
            return new TreeNodeWithChange(this, isSelfOnlyChange);
        }
    }

    /** A node in a layout tree, that has a change compared to a previous version. */
    @RestrictTo(Scope.LIBRARY_GROUP)
    public static final class TreeNodeWithChange {
        @NonNull private final TreeNode mTreeNode;
        private final boolean mIsSelfOnlyChange;

        TreeNodeWithChange(@NonNull TreeNode treeNode, boolean isSelfOnlyChange) {
            this.mTreeNode = treeNode;
            this.mIsSelfOnlyChange = isSelfOnlyChange;
        }

        /**
         * Returns the linear {@link LayoutElement} that this node represents, or null if the node
         * isn't for a {@link LayoutElement}.
         */
        @Nullable
        @RestrictTo(Scope.LIBRARY_GROUP)
        public LayoutElement getLayoutElement() {
            return mTreeNode.mLayoutElement;
        }

        /**
         * Returns the radial {@link ArcLayoutElement} that this node represents, or null if the
         * node isn't for a {@link ArcLayoutElement}.
         */
        @Nullable
        @RestrictTo(Scope.LIBRARY_GROUP)
        public ArcLayoutElement getArcLayoutElement() {
            return mTreeNode.mArcLayoutElement;
        }

        /** Returns the fingerprint for this node. */
        @NonNull
        @RestrictTo(Scope.LIBRARY_GROUP)
        public NodeFingerprint getFingerprint() {
            return mTreeNode.mFingerprint;
        }

        /**
         * Returns an ID for this node based on its position in the tree. Only comparable against
         * other position IDs that are generated with {@link #createNodePosId}.
         */
        @NonNull
        @RestrictTo(Scope.LIBRARY_GROUP)
        public String getPosId() {
            return mTreeNode.mPosId;
        }

        /**
         * Returns true if the change in this node affects the node itself only. Otherwise the
         * change affects both the node and its children.
         */
        @RestrictTo(Scope.LIBRARY_GROUP)
        public boolean isSelfOnlyChange() {
            return mIsSelfOnlyChange;
        }
    }

    /** A diff in layout, containing information about the tree nodes that have changed. */
    @RestrictTo(Scope.LIBRARY_GROUP)
    public static final class LayoutDiff {
        @NonNull private final List<TreeNodeWithChange> mChangedNodes;

        LayoutDiff(@NonNull List<TreeNodeWithChange> changedNodes) {
            this.mChangedNodes = changedNodes;
        }

        /**
         * An ordered list of nodes that have changed. A changed node always comes before its
         * changed descendants in this list.
         */
        @NonNull
        @RestrictTo(Scope.LIBRARY_GROUP)
        public List<TreeNodeWithChange> getChangedNodes() {
            return mChangedNodes;
        }
    }

    private ProtoLayoutDiffer() {}

    /**
     * Create an ID for a layout element, based on its position. This can be stored as a tag in the
     * corresponding View and later used with findViewWithTag() to replace changed elements.
     *
     * @param parentPosId Position-based ID of the parent node.
     * @param childIndex Index of this child node. For the first child, use {@link
     *     #FIRST_CHILD_INDEX}, and increment by one for each.
     */
    @SuppressLint("DefaultLocale")
    @NonNull
    @RestrictTo(Scope.LIBRARY_GROUP)
    public static String createNodePosId(@NonNull String parentPosId, int childIndex) {
        return String.format("%s.%d", parentPosId, childIndex + 1);
    }

    /**
     * Given a position ID generated by {@link #createNodePosId} for a node, extract the position ID
     * of that node's parent.
     *
     * @param posId A position ID for a node.
     * @return The position ID of the node's parent or null if the parent ID cannot be generated.
     */
    @Nullable
    @RestrictTo(Scope.LIBRARY_GROUP)
    public static String getParentNodePosId(@NonNull String posId) {
        if (!posId.startsWith(NODE_ID_PREFIX)) {
            return null;
        }
        int separatorIdx = posId.lastIndexOf('.');
        if (separatorIdx <= NODE_ID_PREFIX.length()) {
            return null;
        }
        return posId.substring(0, separatorIdx);
    }

    /**
     * Given a position ID for a node and a position ID for a potential parent node, returns if the
     * node is actually a descendant of that parent node.
     *
     * @param posId Position ID of the potential descendant node.
     * @param parentPosId Position ID of the potential parent node.
     */
    @RestrictTo(Scope.LIBRARY_GROUP)
    public static boolean isDescendantOf(@NonNull String posId, @NonNull String parentPosId) {
        return posId.length() > parentPosId.length() + 1
                && posId.startsWith(parentPosId)
                && posId.charAt(parentPosId.length()) == '.';
    }

    /**
     * Compute the diff from a previous layout tree to a new one.
     *
     * @param prevTreeFingerprint Fingerprint for the previous layout tree.
     * @param layout The new layout.
     * @return The layout diff or null if the diff cannot be computed, which means the whole layout
     *     should be refreshed.
     */
    @Nullable
    @RestrictTo(Scope.LIBRARY_GROUP)
    public static LayoutDiff getDiff(
            @NonNull TreeFingerprint prevTreeFingerprint, @NonNull Layout layout) {
        if (!layout.getFingerprint().hasRoot()) {
            return null;
        }
        NodeFingerprint prevRootFingerprint = prevTreeFingerprint.getRoot();
        TreeNode rootNode =
                TreeNode.ofLayoutElement(
                        layout.getRoot(), layout.getFingerprint().getRoot(), ROOT_NODE_ID);

        List<TreeNodeWithChange> changedNodes = new ArrayList<>();
        try {
            addChangedNodes(prevRootFingerprint, rootNode, changedNodes);
        } catch (InconsistentFingerprintException ignored) {
            return null;
        }

        return new LayoutDiff(changedNodes);
    }

    /** Check whether 2 nodes represented by the given fingerprints are equivalent. */
    @RestrictTo(Scope.LIBRARY_GROUP)
    public static boolean areNodesEquivalent(
            @NonNull NodeFingerprint nodeA, @NonNull NodeFingerprint nodeB) {
        return getChangeType(nodeA, nodeB) == NodeChangeType.NO_CHANGE;
    }

    private static void addChangedNodes(
            @NonNull NodeFingerprint prevNodeFingerprint,
            @NonNull TreeNode node,
            @NonNull List<TreeNodeWithChange> changedNodes)
            throws InconsistentFingerprintException {
        switch (getChangeType(prevNodeFingerprint, node.mFingerprint)) {
            case CHANGE_IN_SELF_ONLY:
                changedNodes.add(node.withChange(/* isSelfOnlyChange= */ true));
                break;
            case CHANGE_IN_SELF_AND_ALL_CHILDREN:
                changedNodes.add(node.withChange(/* isSelfOnlyChange= */ false));
                break;
            case CHANGE_IN_SELF_AND_SOME_CHILDREN:
                changedNodes.add(node.withChange(/* isSelfOnlyChange= */ true));
                addChangedChildNodes(prevNodeFingerprint, node, changedNodes);
                break;
            case CHANGE_IN_CHILDREN:
                addChangedChildNodes(prevNodeFingerprint, node, changedNodes);
                break;
            case NO_CHANGE:
                break;
        }
    }

    @NonNull
    private static NodeChangeType getChangeType(
            @NonNull NodeFingerprint prevNode, @Nullable NodeFingerprint node) {
        if (node == null) {
            return NodeChangeType.CHANGE_IN_SELF_AND_ALL_CHILDREN;
        }
        if (prevNode.getSelfTypeValue() != node.getSelfTypeValue()) {
            // If the type changes, update everything.
            return NodeChangeType.CHANGE_IN_SELF_AND_ALL_CHILDREN;
        }
        if (node.getSelfPropsValue() == DISCARDED_FINGERPRINT_VALUE
                && node.getChildNodesValue() == DISCARDED_FINGERPRINT_VALUE) {
            if (node.getChildNodesCount() == 0) {
                // Self and children are discarded.
                return NodeChangeType.CHANGE_IN_SELF_AND_ALL_CHILDREN;
            } else {
                // Self is discarded, but children are not discarded at this level. At least one
                // child is discarded though.
                return NodeChangeType.CHANGE_IN_SELF_AND_SOME_CHILDREN;
            }
        }
        if (prevNode.getChildNodesCount() != node.getChildNodesCount()) {
            if (UPDATE_ALL_CHILDREN_AFTER_ADD_REMOVE) {
                return NodeChangeType.CHANGE_IN_SELF_AND_ALL_CHILDREN;
            } else {

                throw new UnsupportedOperationException();
            }
        }
        boolean selfChanged =
                node.getSelfPropsValue() == DISCARDED_FINGERPRINT_VALUE
                        || prevNode.getSelfPropsValue() != node.getSelfPropsValue();
        boolean childrenChanged =
                node.getChildNodesValue() == DISCARDED_FINGERPRINT_VALUE
                        || prevNode.getChildNodesValue() != node.getChildNodesValue();
        if (selfChanged && childrenChanged) {
            return NodeChangeType.CHANGE_IN_SELF_AND_SOME_CHILDREN;
        } else if (selfChanged) {
            return NodeChangeType.CHANGE_IN_SELF_ONLY;
        } else if (childrenChanged) {
            return NodeChangeType.CHANGE_IN_CHILDREN;
        } else {
            return NodeChangeType.NO_CHANGE;
        }
    }

    private static void addChangedChildNodes(
            @NonNull NodeFingerprint prevNodeFingerprint,
            @NonNull TreeNode node,
            @NonNull List<TreeNodeWithChange> changedNodes)
            throws InconsistentFingerprintException {
        List<TreeNode> childList = getChildNodes(node);
        if (childList.isEmpty()) {
            return;
        }
        // This must have been checked in getChangeType()
        checkState(childList.size() == prevNodeFingerprint.getChildNodesCount());
        for (int i = 0; i < childList.size(); i++) {
            TreeNode childNode = childList.get(i);
            NodeFingerprint prevChildNodeFingerprint = prevNodeFingerprint.getChildNodes(i);
            addChangedNodes(prevChildNodeFingerprint, childNode, changedNodes);
        }
    }

    @SuppressWarnings("MixedMutabilityReturnType")
    @NonNull
    private static List<TreeNode> getChildNodes(@NonNull TreeNode node)
            throws InconsistentFingerprintException {
        @Nullable LayoutElement layoutElement = node.mLayoutElement;
        if (layoutElement == null) {
            // Only LayoutElement objects (which includes Arc and Span) can have children.
            return Collections.emptyList();
        }
        NodeFingerprint fingerprint = node.mFingerprint;
        switch (layoutElement.getInnerCase()) {
            case BOX:
                return getLinearChildNodes(
                        layoutElement.getBox().getContentsList(),
                        fingerprint.getChildNodesList(),
                        node.mPosId);
            case COLUMN:
                return getLinearChildNodes(
                        layoutElement.getColumn().getContentsList(),
                        fingerprint.getChildNodesList(),
                        node.mPosId);
            case ROW:
                return getLinearChildNodes(
                        layoutElement.getRow().getContentsList(),
                        fingerprint.getChildNodesList(),
                        node.mPosId);
            case ARC:
                return getRadialChildNodes(
                        layoutElement.getArc().getContentsList(),
                        fingerprint.getChildNodesList(),
                        node.mPosId);
            default:
                return Collections.emptyList();
        }
    }

    @SuppressWarnings("MixedMutabilityReturnType")
    @NonNull
    private static List<TreeNode> getLinearChildNodes(
            @NonNull List<LayoutElement> childElements,
            @NonNull List<NodeFingerprint> childElementFingerprints,
            @NonNull String parentPosId)
            throws InconsistentFingerprintException {
        if (childElements.isEmpty()) {
            return Collections.emptyList();
        }
        if (childElements.size() != childElementFingerprints.size()) {
            throw new InconsistentFingerprintException();
        }
        List<TreeNode> nodes = new ArrayList<>(childElements.size());
        for (int i = 0; i < childElements.size(); i++) {
            String childPosId = createNodePosId(parentPosId, FIRST_CHILD_INDEX + i);
            nodes.add(
                    TreeNode.ofLayoutElement(
                            childElements.get(i), childElementFingerprints.get(i), childPosId));
        }
        return nodes;
    }

    @SuppressWarnings("MixedMutabilityReturnType")
    @NonNull
    private static List<TreeNode> getRadialChildNodes(
            @NonNull List<ArcLayoutElement> childElements,
            @NonNull List<NodeFingerprint> childElementFingerprints,
            @NonNull String parentPosId)
            throws InconsistentFingerprintException {
        if (childElements.isEmpty()) {
            return Collections.emptyList();
        }
        if (childElements.size() != childElementFingerprints.size()) {
            throw new InconsistentFingerprintException();
        }
        List<TreeNode> nodes = new ArrayList<>(childElements.size());
        for (int i = 0; i < childElements.size(); i++) {
            String childPosId = createNodePosId(parentPosId, FIRST_CHILD_INDEX + i);
            nodes.add(
                    TreeNode.ofArcLayoutElement(
                            childElements.get(i), childElementFingerprints.get(i), childPosId));
        }
        return nodes;
    }
}