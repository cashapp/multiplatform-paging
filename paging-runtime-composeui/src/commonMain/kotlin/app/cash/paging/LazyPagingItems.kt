@file:JvmName("ComposeCommonLazyPagingItems")

package app.cash.paging

import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import app.cash.paging.compose.LazyPagingItems
import kotlinx.coroutines.flow.Flow
import kotlin.jvm.JvmName

/**
 * Adds the [LazyPagingItems] and their content to the scope. The range from 0 (inclusive) to
 * [LazyPagingItems.itemCount] (exclusive) always represents the full range of presentable items,
 * because every event from [PagingDataDiffer] will trigger a recomposition.
 *
 * @param items the items received from a [Flow] of [PagingData].
 * @param key a factory of stable and unique keys representing the item. Using the same key
 * for multiple items in the list is not allowed. Type of the key should be saveable
 * via Bundle on Android. If null is passed the position in the list will represent the key.
 * When you specify the key the scroll position will be maintained based on the key, which
 * means if you add/remove items before the current visible item the item with the given key
 * will be kept as the first visible one.
 * @param itemContent the content displayed by a single item. In case the item is `null`, the
 * [itemContent] method should handle the logic of displaying a placeholder instead of the main
 * content displayed by an item which is not `null`.
 */
fun <T : Any> LazyListScope.items(
  items: LazyPagingItems<T>,
  key: ((item: T) -> Any)? = null,
  itemContent: @Composable LazyItemScope.(value: T?) -> Unit,
) {
  items(
    count = items.itemCount,
    key = if (key == null) {
      null
    } else {
      { index ->
        val item = items.peek(index)
        if (item == null) {
          createPagingPlaceholderKey(index)
        } else {
          key(item)
        }
      }
    },
  ) { index ->
    itemContent(items[index])
  }
}

/**
 * Adds the [LazyPagingItems] and their content to the scope where the content of an item is
 * aware of its local index. The range from 0 (inclusive) to [LazyPagingItems.itemCount] (exclusive)
 * always represents the full range of presentable items, because every event from
 * [PagingDataDiffer] will trigger a recomposition.
 *
 * @param items the items received from a [Flow] of [PagingData].
 * @param key a factory of stable and unique keys representing the item. Using the same key
 * for multiple items in the list is not allowed. Type of the key should be saveable
 * via Bundle on Android. If null is passed the position in the list will represent the key.
 * When you specify the key the scroll position will be maintained based on the key, which
 * means if you add/remove items before the current visible item the item with the given key
 * will be kept as the first visible one.
 * @param itemContent the content displayed by a single item. In case the item is `null`, the
 * [itemContent] method should handle the logic of displaying a placeholder instead of the main
 * content displayed by an item which is not `null`.
 */
fun <T : Any> LazyListScope.itemsIndexed(
  items: LazyPagingItems<T>,
  key: ((index: Int, item: T) -> Any)? = null,
  itemContent: @Composable LazyItemScope.(index: Int, value: T?) -> Unit,
) {
  items(
    count = items.itemCount,
    key = if (key == null) {
      null
    } else {
      { index ->
        val item = items.peek(index)
        if (item == null) {
          createPagingPlaceholderKey(index)
        } else {
          key(index, item)
        }
      }
    },
  ) { index ->
    itemContent(index, items[index])
  }
}
