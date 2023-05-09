package app.cash.paging

import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import app.cash.paging.compose.LazyPagingItems

@Deprecated(
  message = "Call LazyListScope.items directly with LazyPagingItems #itemKey and" +
    "#itemContentType helper functions.",
  replaceWith = ReplaceWith(
    expression = """items(
           count = items.itemCount,
           key = items.itemKey(key),
           contentType = items.itemContentType(
                contentType
           )
        ) { index ->
            val item = items[index]
            itemContent(item)
        }""",
  ),
)
expect fun <T : Any> LazyListScope.items(
  items: LazyPagingItems<T>,
  key: ((item: T) -> Any)?, /*  = null */
  itemContent: @Composable LazyItemScope.(value: T?) -> Unit,
)

fun <T : Any> LazyListScope.items(
  items: LazyPagingItems<T>,
  itemContent: @Composable LazyItemScope.(value: T?) -> Unit,
) = items(items, null, itemContent)

@Deprecated(
  message = "Deprecating support for indexed keys on non-null items as it is susceptible to" +
    "errors when items indices shift due to prepends. Call LazyListScope.items directly" +
    "with LazyPagingItems #itemKey and #itemContentType helper functions.",
  replaceWith = ReplaceWith(
    expression = """items(
           count = items.itemCount,
           key = items.itemKey(key),
           contentType = items.itemContentType(
                contentType
           )
        ) { index ->
            val item = items[index]
            itemContent(item)
        }""",
  ),
)
expect fun <T : Any> LazyListScope.itemsIndexed(
  items: LazyPagingItems<T>,
  key: ((index: Int, item: T) -> Any)?, /*  = null */
  itemContent: @Composable LazyItemScope.(index: Int, value: T?) -> Unit,
)

fun <T : Any> LazyListScope.itemsIndexed(
  items: LazyPagingItems<T>,
  itemContent: @Composable LazyItemScope.(index: Int, value: T?) -> Unit,
) = itemsIndexed(items, null, itemContent)
