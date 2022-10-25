/*
 * Copyright 2021 The Android Open Source Project
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

package androidx.paging

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListUpdateCallback
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * For some tests, this test uses a real recyclerview with a real adapter to serve as an
 * integration test so that we can validate all updates and state restorations after updates.
 */
class NullPaddedListDiffWithRecyclerViewTest {

    @Test
    fun distinctLists_validateDiff() {
        val pre = NullPaddedStorage(
            placeholdersBefore = 10,
            data = createItems(10, 10), // their positions won't be in the new list
            placeholdersAfter = 20
        )
        val post = NullPaddedStorage(
            placeholdersBefore = 0,
            data = createItems(100, 1),
            placeholdersAfter = 0
        )
        updateDiffTest(pre, post)
    }

    @Test
    fun random_distinctListTest() {
        // this is a random test but if it fails, the exception will have enough information to
        // create an isolated test
        val rand = Random
        fun randomNullPaddedStorage(startId: Int) = NullPaddedStorage(
            placeholdersBefore = rand.nextInt(0, 20),
            data = createItems(
                startId = startId,
                count = rand.nextInt(0, 20)
            ),
            placeholdersAfter = rand.nextInt(0, 20)
        )
        repeat(RANDOM_TEST_REPEAT_SIZE) {
            updateDiffTest(
                pre = randomNullPaddedStorage(0),
                post = randomNullPaddedStorage(1_000)
            )
        }
    }

    @Test
    fun continuousMatch_1() {
        val pre = NullPaddedStorage(
            placeholdersBefore = 4,
            data = createItems(startId = 0, count = 16),
            placeholdersAfter = 1
        )
        val post = NullPaddedStorage(
            placeholdersBefore = 1,
            data = createItems(startId = 13, count = 4),
            placeholdersAfter = 19
        )
        updateDiffTest(pre, post)
    }

    @Test
    fun continuousMatch_2() {
        val pre = NullPaddedStorage(
            placeholdersBefore = 6,
            data = createItems(startId = 0, count = 9),
            placeholdersAfter = 19
        )
        val post = NullPaddedStorage(
            placeholdersBefore = 14,
            data = createItems(startId = 4, count = 3),
            placeholdersAfter = 11
        )
        updateDiffTest(pre, post)
    }

    @Test
    fun continuousMatch_3() {
        val pre = NullPaddedStorage(
            placeholdersBefore = 11,
            data = createItems(startId = 0, count = 4),
            placeholdersAfter = 6
        )
        val post = NullPaddedStorage(
            placeholdersBefore = 7,
            data = createItems(startId = 0, count = 1),
            placeholdersAfter = 11
        )
        updateDiffTest(pre, post)
    }

    @Test
    fun continuousMatch_4() {
        val pre = NullPaddedStorage(
            placeholdersBefore = 4,
            data = createItems(startId = 0, count = 15),
            placeholdersAfter = 18
        )
        val post = NullPaddedStorage(
            placeholdersBefore = 11,
            data = createItems(startId = 5, count = 17),
            placeholdersAfter = 9
        )
        updateDiffTest(pre, post)
    }

    @Test
    fun randomTest_withContinuousMatch() {
        randomContinuousMatchTest(shuffle = false)
    }

    @Test
    fun randomTest_withContinuousMatch_withShuffle() {
        randomContinuousMatchTest(shuffle = true)
    }

    /**
     * Tests that if two lists have some overlaps, we dispatch the right diff events.
     * It can also optionally shuffle the lists.
     */
    private fun randomContinuousMatchTest(shuffle: Boolean) {
        // this is a random test but if it fails, the exception will have enough information to
        // create an isolated test
        val rand = Random
        fun randomNullPaddedStorage(startId: Int) = NullPaddedStorage(
            placeholdersBefore = rand.nextInt(0, 20),
            data = createItems(
                startId = startId,
                count = rand.nextInt(0, 20)
            ).let {
                if (shuffle) it.shuffled()
                else it
            },
            placeholdersAfter = rand.nextInt(0, 20)
        )
        repeat(RANDOM_TEST_REPEAT_SIZE) {
            val pre = randomNullPaddedStorage(0)
            val post = randomNullPaddedStorage(
                startId = if (pre.storageCount > 0) {
                    pre.getFromStorage(rand.nextInt(pre.storageCount)).id
                } else {
                    0
                }
            )
            updateDiffTest(
                pre = pre,
                post = post
            )
        }
    }

    /**
     * Validates that the update events between [pre] and [post] are correct.
     */
    private fun updateDiffTest(
        pre: NullPaddedStorage,
        post: NullPaddedStorage
    ) {
        val callback = ValidatingListUpdateCallback(pre, post)
        val diffResult = pre.computeDiff(post, NullPaddedListItem.CALLBACK)
        pre.dispatchDiff(callback, post, diffResult)
        callback.validateRunningListAgainst()
    }

    private data class NullPaddedListItem(
        val id: Int,
        val value: String
    ) {
        companion object {
            val CALLBACK = object : DiffUtil.ItemCallback<NullPaddedListItem>() {
                override fun areItemsTheSame(
                    oldItem: NullPaddedListItem,
                    newItem: NullPaddedListItem
                ): Boolean {
                    return oldItem.id == newItem.id
                }

                override fun areContentsTheSame(
                    oldItem: NullPaddedListItem,
                    newItem: NullPaddedListItem
                ): Boolean {
                    return oldItem == newItem
                }
            }
        }
    }

    private data class UIItemSnapshot(
        // top coordinate of the item
        val top: Int,
        // the item it is bound to, unless it was a placeholder
        val boundItem: NullPaddedListItem?,
        // the position it was bound to
        val boundPos: Int
    )

    private class NullPaddedStorage(
        override val placeholdersBefore: Int,
        private val data: List<NullPaddedListItem>,
        override val placeholdersAfter: Int
    ) : NullPaddedList<NullPaddedListItem> {
        private val stringRepresentation by lazy {
            """
            $placeholdersBefore:${data.size}:$placeholdersAfter
            $data
            """.trimIndent()
        }

        override fun getFromStorage(localIndex: Int): NullPaddedListItem = data[localIndex]

        override val size: Int
            get() = placeholdersBefore + data.size + placeholdersAfter

        override val storageCount: Int
            get() = data.size

        override fun toString() = stringRepresentation
    }

    private fun createItems(
        startId: Int,
        count: Int
    ): List<NullPaddedListItem> {
        return (startId until startId + count).map {
            NullPaddedListItem(
                id = it,
                value = "$it"
            )
        }
    }

    /**
     * Creates an expected UI snapshot based on the given list and scroll position / offset.
     */
    private fun createExpectedSnapshot(
        firstItemTopOffset: Int = 0,
        startItemIndex: Int,
        backingList: NullPaddedList<NullPaddedListItem>
    ): List<UIItemSnapshot> {
        check(firstItemTopOffset <= 0) {
            "first item offset should not be negative"
        }
        var remainingHeight = RV_HEIGHT - firstItemTopOffset
        var pos = startItemIndex
        var top = firstItemTopOffset
        val result = mutableListOf<UIItemSnapshot>()
        while (remainingHeight > 0 && pos < backingList.size) {
            result.add(
                UIItemSnapshot(
                    top = top,
                    boundItem = backingList.get(pos),
                    boundPos = pos
                )
            )
            top += ITEM_HEIGHT
            remainingHeight -= ITEM_HEIGHT
            pos++
        }
        return result
    }

    /**
     * A ListUpdateCallback implementation that tracks all change notifications and then validate
     * that
     * a) changes are correct
     * b) no unnecessary events are dispatched (e.g. dispatching change for an item then removing
     * it)
     */
    private class ValidatingListUpdateCallback<T>(
        previousList: NullPaddedList<T>?,
        private val newList: NullPaddedList<T>
    ) : ListUpdateCallback {
        // used in assertion messages
        val msg = """
                oldList: $previousList
                newList: $newList
        """.trimIndent()

        // all changes are applied to this list, at the end, we'll validate against the new list
        // to ensure all updates made sense and no unnecessary updates are made
        val runningList: MutableList<ListSnapshotItem> =
            previousList?.createSnapshot() ?: mutableListOf()

        private val size: Int
            get() = runningList.size

        private fun Int.assertWithinBounds() {
            assertTrue(this >= 0, msg)
            assertTrue(this <= size, msg)
        }

        override fun onInserted(position: Int, count: Int) {
            position.assertWithinBounds()
            assertTrue(count >= 1, msg)
            repeat(count) {
                runningList.add(position, ListSnapshotItem.Inserted)
            }
        }

        override fun onRemoved(position: Int, count: Int) {
            position.assertWithinBounds()
            (position + count).assertWithinBounds()
            assertTrue(count >= 1, msg)
            (position until position + count).forEach { pos ->
                assertTrue(
                    runningList[pos].isOriginalItem(),
                    "$msg\nshouldn't be removing an item that already got a change event" +
                        " pos: $pos , ${runningList[pos]}"
                )
            }
            repeat(count) {
                runningList.removeAt(position)
            }
        }

        override fun onMoved(fromPosition: Int, toPosition: Int) {
            fromPosition.assertWithinBounds()
            toPosition.assertWithinBounds()
            runningList.add(toPosition, runningList.removeAt(fromPosition))
        }

        override fun onChanged(position: Int, count: Int, payload: Any?) {
            position.assertWithinBounds()
            (position + count).assertWithinBounds()
            assertTrue(count >= 1, msg)
            (position until position + count).forEach { pos ->
                // make sure we don't dispatch overlapping updates
                assertTrue(
                    runningList[pos].isOriginalItem(),
                    "$msg\nunnecessary change event for position $pos $payload " +
                        "${runningList[pos]}"
                )
                if (payload == DiffingChangePayload.PLACEHOLDER_TO_ITEM ||
                    payload == DiffingChangePayload.PLACEHOLDER_POSITION_CHANGE
                ) {
                    assertIs<ListSnapshotItem.Placeholder>(runningList[pos], msg)
                } else {
                    assertIs<ListSnapshotItem.Item<*>>(runningList[pos], msg)
                }
                runningList[pos] = ListSnapshotItem.Changed(
                    payload = payload as? DiffingChangePayload
                )
            }
        }

        fun validateRunningListAgainst() {
            // check for size first
            assertEquals(newList.size, size, msg)
            val newListSnapshot = newList.createSnapshot()
            runningList.forEachIndexed { index, listSnapshotItem ->
                val newListItem = newListSnapshot[index]
                listSnapshotItem.assertReplacement(
                    msg,
                    newListItem
                )
                if (!listSnapshotItem.isOriginalItem()) {
                    // if it changed, replace from new snapshot
                    runningList[index] = newListSnapshot[index]
                }
            }
            // now after this, each list must be exactly equal, if not, something is wrong
            assertContentEquals(newListSnapshot, runningList, msg)
        }
    }

    companion object {
        private const val RV_HEIGHT = 100
        private const val ITEM_HEIGHT = 10
        private const val RANDOM_TEST_REPEAT_SIZE = 1_000
    }
}

private fun <T> NullPaddedList<T>.get(index: Int): T? {
    if (index < placeholdersBefore) return null
    val storageIndex = index - placeholdersBefore
    if (storageIndex >= storageCount) return null
    return getFromStorage(storageIndex)
}

/**
 * Create a snapshot of this current that can be used to verify diffs.
 */
private fun <T> NullPaddedList<T>.createSnapshot(): MutableList<ListSnapshotItem> = (0 until size)
    .mapTo(mutableListOf()) { pos ->
        get(pos)?.let {
            ListSnapshotItem.Item(it)
        } ?: ListSnapshotItem.Placeholder(pos)
    }

/**
 * Sealed classes to identify items in the list.
 */
internal sealed class ListSnapshotItem {
    // means the item didn't change at all in diffs.
    fun isOriginalItem() = this is Item<*> || this is Placeholder

    /**
     * Asserts that this item properly represents the replacement (newListItem).
     */
    abstract fun assertReplacement(
        msg: String,
        newListItem: ListSnapshotItem
    )

    data class Item<T>(val item: T) : ListSnapshotItem() {
        override fun assertReplacement(
            msg: String,
            newListItem: ListSnapshotItem
        ) {
            // no change
            assertEquals(this, newListItem, msg)
        }
    }

    data class Placeholder(val pos: Int) : ListSnapshotItem() {
        override fun assertReplacement(
            msg: String,
            newListItem: ListSnapshotItem
        ) {
            assertIs<Placeholder>(newListItem, msg)
            val replacement = newListItem as Placeholder
            // make sure position didn't change. If it did, we would be replaced with a [Changed].
            assertEquals(replacement.pos, pos, msg)
        }
    }

    /**
     * Inserted into the list when we receive a change notification about an item/placeholder.
     */
    data class Changed(val payload: DiffingChangePayload?) : ListSnapshotItem() {
        override fun assertReplacement(
            msg: String,
            newListItem: ListSnapshotItem
        ) {
            // there are 4 cases for changes.
            // is either placeholder -> placeholder with new position
            // placeholder to item
            // item to placeholder
            // item change from original diffing.
            when (payload) {
                DiffingChangePayload.ITEM_TO_PLACEHOLDER -> {
                    assertIs<Placeholder>(newListItem, msg)
                }
                DiffingChangePayload.PLACEHOLDER_TO_ITEM -> {
                    assertIs<Item<*>>(newListItem, msg)
                }
                DiffingChangePayload.PLACEHOLDER_POSITION_CHANGE -> {
                    assertIs<Placeholder>(newListItem, msg)
                }
                else -> {
                    // item change that came from diffing.
                    assertIs<Item<*>>(newListItem, msg)
                }
            }
        }
    }

    /**
     * Used when an item/placeholder is inserted to the list
     */
    object Inserted : ListSnapshotItem() {
        override fun assertReplacement(msg: String, newListItem: ListSnapshotItem) {
            // nothing to assert here, it can represent anything in the new list.
        }
    }
}
