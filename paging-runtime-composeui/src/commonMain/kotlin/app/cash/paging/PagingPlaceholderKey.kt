package app.cash.paging

//@Parcelize Uncomment and remove createPagingPlaceholderKey once KTIJ-24082 is fixed
abstract class PagingPlaceholderKey: Parcelable {
  abstract val index: Int
}

expect fun createPagingPlaceholderKey(index: Int): PagingPlaceholderKey
