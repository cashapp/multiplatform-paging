package app.cash.paging

import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import app.cash.paging.compose.LazyPagingItems

expect fun <T : Any> LazyListScope.items(
  items: LazyPagingItems<T>,
  key: ((item: T) -> Any)?, /*  = null */
  itemContent: @Composable LazyItemScope.(value: T?) -> Unit,
)

fun <T : Any> LazyListScope.items(
  items: LazyPagingItems<T>,
  itemContent: @Composable LazyItemScope.(value: T?) -> Unit,
) = items(items, null, itemContent)

expect fun <T : Any> LazyListScope.itemsIndexed(
  items: LazyPagingItems<T>,
  key: ((index: Int, item: T) -> Any)?, /*  = null */
  itemContent: @Composable LazyItemScope.(index: Int, value: T?) -> Unit,
)

fun <T : Any> LazyListScope.itemsIndexed(
  items: LazyPagingItems<T>,
  itemContent: @Composable LazyItemScope.(index: Int, value: T?) -> Unit,
) = itemsIndexed(items, null, itemContent)
