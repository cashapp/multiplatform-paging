package app.cash.paging

import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.paging.compose.items
import androidx.paging.compose.itemsIndexed
import app.cash.paging.compose.LazyPagingItems

actual inline fun <T : Any> LazyListScope.items(
  items: LazyPagingItems<T>,
  noinline key: ((item: T) -> Any)?,
  noinline itemContent: @Composable LazyItemScope.(value: T?) -> Unit,
) = items(items, key, itemContent)

actual inline fun <T : Any> LazyListScope.itemsIndexed(
  items: LazyPagingItems<T>,
  noinline key: ((index: Int, item: T) -> Any)?,
  noinline itemContent: @Composable LazyItemScope.(index: Int, value: T?) -> Unit,
) = itemsIndexed(items, key, itemContent)
