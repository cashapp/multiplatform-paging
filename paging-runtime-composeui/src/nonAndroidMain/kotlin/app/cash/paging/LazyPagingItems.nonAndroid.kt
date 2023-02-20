package app.cash.paging

import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import app.cash.paging.compose.LazyPagingItems

actual fun <T : Any> LazyListScope.items(
  items: LazyPagingItems<T>,
  key: ((item: T) -> Any)?,
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
          PagingPlaceholderKey(index)
        } else {
          key(item)
        }
      }
    },
  ) { index ->
    itemContent(items[index])
  }
}

actual fun <T : Any> LazyListScope.itemsIndexed(
  items: LazyPagingItems<T>,
  key: ((index: Int, item: T) -> Any)?,
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
          PagingPlaceholderKey(index)
        } else {
          key(index, item)
        }
      }
    },
  ) { index ->
    itemContent(index, items[index])
  }
}

private data class PagingPlaceholderKey(
  val index: Int,
)
