package app.cash.paging

import androidx.paging.CombinedLoadStates
import androidx.paging.ItemSnapshotList
import androidx.paging.PagingData
import androidx.paging.PagingDataEvent
import androidx.paging.PagingDataPresenter
import androidx.recyclerview.widget.DiffUtil
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import platform.UIKit.UICollectionView
import platform.darwin.NSInteger

// Making abstract causes the compilation error "Non-final Kotlin subclasses of Objective-C classes are not yet supported".
class PagingCollectionViewController<T : Any> {

  private val mainDispatcher: CoroutineDispatcher = Dispatchers.Main
  private val workerDispatcher: CoroutineDispatcher = Dispatchers.Default

  private var collectionView: UICollectionView? = null

  private val presenter = object : PagingDataPresenter<T>(
    mainDispatcher,
      TODO(),
  ) {
    override suspend fun presentPagingDataEvent(event: PagingDataEvent<T>) {
      TODO("Not yet implemented")
    }
  }/*
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
  )*/

  suspend fun submitData(pagingData: PagingData<T>) {
    presenter.collectFrom(pagingData)
  }

  fun retry() {
    presenter.retry()
  }

  fun refresh() {
    presenter.refresh()
  }

  protected fun getItem(position: Int) = presenter[position]

  fun peek(index: Int) = presenter.peek(index)

  fun snapshot(): ItemSnapshotList<T> = presenter.snapshot()

  fun collectionView(collectionView: UICollectionView, numberOfItemsInSection: NSInteger): NSInteger {
    this.collectionView = collectionView
    return presenter.size.toLong()
  }

  val loadStateFlow: Flow<CombinedLoadStates> = presenter.loadStateFlow

  val onPagesUpdatedFlow: Flow<Unit> = presenter.onPagesUpdatedFlow

  fun addLoadStateListener(listener: (CombinedLoadStates) -> Unit) {
    presenter.addLoadStateListener(listener)
  }

  fun removeLoadStateListener(listener: (CombinedLoadStates) -> Unit) {
    presenter.removeLoadStateListener(listener)
  }

  fun addOnPagesUpdatedListener(listener: () -> Unit) {
    presenter.addOnPagesUpdatedListener(listener)
  }

  fun removeOnPagesUpdatedListener(listener: () -> Unit) {
    presenter.removeOnPagesUpdatedListener(listener)
  }
}
