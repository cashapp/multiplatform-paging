package app.cash.paging

actual fun createPagingPlaceholderKey(
  index: Int,
): PagingPlaceholderKey = JvmPagingPlaceholderKey(index)

private data class JvmPagingPlaceholderKey(
  override val index: Int,
) : PagingPlaceholderKey(), Parcelable
