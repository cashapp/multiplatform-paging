package app.cash.paging

import android.os.Parcelable

actual fun createPagingPlaceholderKey(
  index: Int,
): PagingPlaceholderKey = ParcelizedPagingPlaceholderKey(index)

@Parcelize
private data class ParcelizedPagingPlaceholderKey(
  override val index: Int,
) : PagingPlaceholderKey(), Parcelable
