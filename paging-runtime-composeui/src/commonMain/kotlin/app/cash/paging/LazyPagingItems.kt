package app.cash.paging

import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * The class responsible for accessing the data from a [Flow] of [PagingData].
 * In order to obtain an instance of [LazyPagingItems] use the [collectAsLazyPagingItems] extension
 * method of [Flow] with [PagingData].
 * This instance can be used by the [items] and [itemsIndexed] methods inside [LazyListScope] to
 * display data received from the [Flow] of [PagingData].
 *
 * @param T the type of value used by [PagingData].
 */
class LazyPagingItems<T : Any> internal constructor(
  /**
   * the [Flow] object which contains a stream of [PagingData] elements.
   */
  private val flow: Flow<PagingData<T>>
) {
  private val mainDispatcher = Dispatchers.Main

  /**
   * Contains the immutable [ItemSnapshotList] of currently presented items, including any
   * placeholders if they are enabled.
   * Note that similarly to [peek] accessing the items in a list will not trigger any loads.
   * Use [get] to achieve such behavior.
   */
  var itemSnapshotList by mutableStateOf(
    ItemSnapshotList<T>(0, 0, emptyList())
  )
    private set

  /**
   * The number of items which can be accessed.
   */
  val itemCount: Int get() = itemSnapshotList.size

  private val differCallback: DifferCallback = object : DifferCallback {
    override fun onChanged(position: Int, count: Int) {
      if (count > 0) {
        updateItemSnapshotList()
      }
    }

    override fun onInserted(position: Int, count: Int) {
      if (count > 0) {
        updateItemSnapshotList()
      }
    }

    override fun onRemoved(position: Int, count: Int) {
      if (count > 0) {
        updateItemSnapshotList()
      }
    }
  }

  private val pagingDataDiffer = object : PagingDataDiffer<T>(
    differCallback = differCallback,
    mainContext = mainDispatcher
  ) {
    override suspend fun presentNewList(
      previousList: NullPaddedList<T>,
      newList: NullPaddedList<T>,
      lastAccessedIndex: Int,
      onListPresentable: () -> Unit
    ): Int? {
      onListPresentable()
      updateItemSnapshotList()
      return null
    }
  }

  private fun updateItemSnapshotList() {
    itemSnapshotList = pagingDataDiffer.snapshot()
  }

  /**
   * Returns the presented item at the specified position, notifying Paging of the item access to
   * trigger any loads necessary to fulfill prefetchDistance.
   *
   * @see peek
   */
  operator fun get(index: Int): T? {
    pagingDataDiffer[index] // this registers the value load
    return itemSnapshotList[index]
  }

  /**
   * Returns the presented item at the specified position, without notifying Paging of the item
   * access that would normally trigger page loads.
   *
   * @param index Index of the presented item to return, including placeholders.
   * @return The presented item at position [index], `null` if it is a placeholder
   */
  fun peek(index: Int): T? {
    return itemSnapshotList[index]
  }

  /**
   * Retry any failed load requests that would result in a [LoadStateError] update to this
   * [LazyPagingItems].
   *
   * Unlike [refresh], this does not invalidate [PagingSource], it only retries failed loads
   * within the same generation of [PagingData].
   *
   * [LoadStateError] can be generated from two types of load requests:
   *  * [PagingSource.load] returning [PagingSource.LoadResult.Error]
   *  * [RemoteMediator.load] returning [RemoteMediator.MediatorResult.Error]
   */
  fun retry() {
    pagingDataDiffer.retry()
  }

  /**
   * Refresh the data presented by this [LazyPagingItems].
   *
   * [refresh] triggers the creation of a new [PagingData] with a new instance of [PagingSource]
   * to represent an updated snapshot of the backing dataset. If a [RemoteMediator] is set,
   * calling [refresh] will also trigger a call to [RemoteMediator.load] with [LoadType] [REFRESH]
   * to allow [RemoteMediator] to check for updates to the dataset backing [PagingSource].
   *
   * Note: This API is intended for UI-driven refresh signals, such as swipe-to-refresh.
   * Invalidation due repository-layer signals, such as DB-updates, should instead use
   * [PagingSource.invalidate].
   *
   * @see PagingSource.invalidate
   */
  fun refresh() {
    pagingDataDiffer.refresh()
  }

  /**
   * A [CombinedLoadStates] object which represents the current loading state.
   */
  var loadState: CombinedLoadStates by mutableStateOf(
    CombinedLoadStates(
      refresh = InitialLoadStates.refresh,
      prepend = InitialLoadStates.prepend,
      append = InitialLoadStates.append,
      source = InitialLoadStates
    )
  )
    private set

  internal suspend fun collectLoadState() {
    pagingDataDiffer.loadStateFlow.filterNotNull().collect {
      loadState = it
    }
  }

  internal suspend fun collectPagingData() {
    flow.collectLatest {
      pagingDataDiffer.collectFrom(it)
    }
  }

  private companion object {
    init {
      /**
       * Implements the Logger interface from paging-common and injects it into the LOGGER
       * global var stored within Pager.
       *
       * Checks for null LOGGER because other runtime entry points to paging can also
       * inject a Logger
       */
      LOGGER = platformLogger()
    }
  }
}

private val IncompleteLoadState = LoadStateNotLoading(false)
private val InitialLoadStates = LoadStates(
  // TODO: casting?
  LoadStateLoading as LoadState,
  IncompleteLoadState as LoadState,
  IncompleteLoadState as LoadState
)

/**
 * Collects values from this [Flow] of [PagingData] and represents them inside a [LazyPagingItems]
 * instance. The [LazyPagingItems] instance can be used by the [items] and [itemsIndexed] methods
 * from [LazyListScope] in order to display the data obtained from a [Flow] of [PagingData].
 *
 * @param context the [CoroutineContext] to perform the collection of [PagingData]
 * and [CombinedLoadStates].
 */
@Composable
fun <T : Any> Flow<PagingData<T>>.collectAsLazyPagingItems(
  context: CoroutineContext = EmptyCoroutineContext
): LazyPagingItems<T> {

  val lazyPagingItems = remember(this) { LazyPagingItems(this) }

  LaunchedEffect(lazyPagingItems) {
    if (context == EmptyCoroutineContext) {
      lazyPagingItems.collectPagingData()
    } else {
      withContext(context) {
        lazyPagingItems.collectPagingData()
      }
    }
  }

  LaunchedEffect(lazyPagingItems) {
    if (context == EmptyCoroutineContext) {
      lazyPagingItems.collectLoadState()
    } else {
      withContext(context) {
        lazyPagingItems.collectLoadState()
      }
    }
  }

  return lazyPagingItems
}

/**
 * Adds the [LazyPagingItems] and their content to the scope. The range from 0 (inclusive) to
 * [LazyPagingItems.itemCount] (exclusive) always represents the full range of presentable items,
 * because every event from [PagingDataDiffer] will trigger a recomposition.
 *
 * @param items the items received from a [Flow] of [PagingData].
 * @param key a factory of stable and unique keys representing the item. Using the same key
 * for multiple items in the list is not allowed. Type of the key should be saveable
 * via Bundle on Android. If null is passed the position in the list will represent the key.
 * When you specify the key the scroll position will be maintained based on the key, which
 * means if you add/remove items before the current visible item the item with the given key
 * will be kept as the first visible one.
 * @param itemContent the content displayed by a single item. In case the item is `null`, the
 * [itemContent] method should handle the logic of displaying a placeholder instead of the main
 * content displayed by an item which is not `null`.
 */
fun <T : Any> LazyListScope.items(
  items: LazyPagingItems<T>,
  key: ((item: T) -> Any)? = null,
  itemContent: @Composable LazyItemScope.(value: T?) -> Unit
) {
  items(
    count = items.itemCount,
    key = if (key == null) null else { index ->
      val item = items.peek(index)
      if (item == null) {
        createPagingPlaceholderKey(index)
      } else {
        key(item)
      }
    }
  ) { index ->
    itemContent(items[index])
  }
}

/**
 * Adds the [LazyPagingItems] and their content to the scope where the content of an item is
 * aware of its local index. The range from 0 (inclusive) to [LazyPagingItems.itemCount] (exclusive)
 * always represents the full range of presentable items, because every event from
 * [PagingDataDiffer] will trigger a recomposition.
 *
 * @param items the items received from a [Flow] of [PagingData].
 * @param key a factory of stable and unique keys representing the item. Using the same key
 * for multiple items in the list is not allowed. Type of the key should be saveable
 * via Bundle on Android. If null is passed the position in the list will represent the key.
 * When you specify the key the scroll position will be maintained based on the key, which
 * means if you add/remove items before the current visible item the item with the given key
 * will be kept as the first visible one.
 * @param itemContent the content displayed by a single item. In case the item is `null`, the
 * [itemContent] method should handle the logic of displaying a placeholder instead of the main
 * content displayed by an item which is not `null`.
 */
fun <T : Any> LazyListScope.itemsIndexed(
  items: LazyPagingItems<T>,
  key: ((index: Int, item: T) -> Any)? = null,
  itemContent: @Composable LazyItemScope.(index: Int, value: T?) -> Unit
) {
  items(
    count = items.itemCount,
    key = if (key == null) null else { index ->
      val item = items.peek(index)
      if (item == null) {
        createPagingPlaceholderKey(index)
      } else {
        key(index, item)
      }
    }
  ) { index ->
    itemContent(index, items[index])
  }
}
