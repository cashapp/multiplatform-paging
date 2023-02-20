@file:JvmName("ComposeCommonLazyPagingItems")

package app.cash.paging

import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import app.cash.paging.compose.LazyPagingItems
import kotlin.jvm.JvmName

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
