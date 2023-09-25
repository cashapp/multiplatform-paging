package app.cash.paging

import androidx.paging.AsyncPagingDataDiffer
import androidx.paging.CombinedLoadStates
import androidx.paging.ItemSnapshotList
import androidx.paging.PagingData
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListUpdateCallback
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import platform.Foundation.NSIndexPath
import platform.UIKit.UICollectionView
import platform.UIKit.indexPathForRow
import platform.darwin.NSInteger

// Making abstract causes the compilation error "Non-final Kotlin subclasses of Objective-C classes are not yet supported".
class PagingCollectionViewController<T : Any> {

  private val mainDispatcher: CoroutineDispatcher = Dispatchers.Main
  private val workerDispatcher: CoroutineDispatcher = Dispatchers.Default

  private var collectionView: UICollectionView? = null

  private val diffCallback = object : DiffUtil.ItemCallback<T>() {
    override fun areItemsTheSame(oldItem: T, newItem: T): Boolean {
      return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: T, newItem: T): Boolean {
      return oldItem == newItem
    }
  }

  private val differ = AsyncPagingDataDiffer(
    diffCallback,
    object : ListUpdateCallback {
      override fun onInserted(position: Int, count: Int) {
        checkNotNull(collectionView)
        collectionView!!.insertItemsAtIndexPaths(List(count) { NSIndexPath.indexPathForRow((it + position).toLong(), 0) })
      }

      override fun onRemoved(position: Int, count: Int) {
        checkNotNull(collectionView)
        collectionView!!.deleteItemsAtIndexPaths(List(count) { NSIndexPath.indexPathForRow((it + position).toLong(), 0) })
      }

      override fun onMoved(fromPosition: Int, toPosition: Int) {
        checkNotNull(collectionView)
        collectionView!!.moveItemAtIndexPath(NSIndexPath.indexPathForRow(fromPosition.toLong(), 0), NSIndexPath.indexPathForRow(toPosition.toLong(), 0))
      }

      override fun onChanged(position: Int, count: Int, payload: Any?) {
        TODO("onChanged(position=$position, count=$count, payload=$payload)")
      }
    },
    mainDispatcher = mainDispatcher,
    workerDispatcher = workerDispatcher,
  )

  suspend fun submitData(pagingData: PagingData<T>) {
    differ.submitData(pagingData)
  }

  fun retry() {
    differ.retry()
  }

  fun refresh() {
    differ.refresh()
  }

  protected fun getItem(position: Int) = differ.getItem(position)

  fun peek(index: Int) = differ.peek(index)

  fun snapshot(): ItemSnapshotList<T> = differ.snapshot()

  fun collectionView(collectionView: UICollectionView, numberOfItemsInSection: NSInteger): NSInteger {
    this.collectionView = collectionView
    return differ.itemCount.toLong()
  }

  val loadStateFlow: Flow<CombinedLoadStates> = differ.loadStateFlow

  val onPagesUpdatedFlow: Flow<Unit> = differ.onPagesUpdatedFlow

  fun addLoadStateListener(listener: (CombinedLoadStates) -> Unit) {
    differ.addLoadStateListener(listener)
  }

  fun removeLoadStateListener(listener: (CombinedLoadStates) -> Unit) {
    differ.removeLoadStateListener(listener)
  }

  fun addOnPagesUpdatedListener(listener: () -> Unit) {
    differ.addOnPagesUpdatedListener(listener)
  }

  fun removeOnPagesUpdatedListener(listener: () -> Unit) {
    differ.removeOnPagesUpdatedListener(listener)
  }
}
