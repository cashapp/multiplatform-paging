/*
 * Copyright 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package app.cash.paging

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlin.coroutines.CoroutineContext

/** @suppress */
expect abstract class PagingDataDiffer<T : Any>(
  differCallback: DifferCallback,
  mainContext: CoroutineContext, /* = Dispatchers.Main */
  cachedPagingData: PagingData<T>?, /* = null */
) {

  abstract suspend fun presentNewList(
    previousList: NullPaddedList<T>,
    newList: NullPaddedList<T>,
    lastAccessedIndex: Int,
    onListPresentable: () -> Unit,
  ): Int?

  open fun postEvents(): Boolean

  suspend fun collectFrom(pagingData: PagingData<T>)

  operator fun get(index: Int): T?

  fun peek(index: Int): T?

  fun snapshot(): ItemSnapshotList<T>

  fun retry()

  fun refresh()

  val size: Int

  val loadStateFlow: StateFlow<CombinedLoadStates?>

  val onPagesUpdatedFlow: Flow<Unit>

  fun addOnPagesUpdatedListener(listener: () -> Unit)

  fun removeOnPagesUpdatedListener(listener: () -> Unit)

  fun addLoadStateListener(listener: (CombinedLoadStates) -> Unit)

  fun removeLoadStateListener(listener: (CombinedLoadStates) -> Unit)
}

/** @suppress */
expect interface DifferCallback {
  fun onChanged(position: Int, count: Int)
  fun onInserted(position: Int, count: Int)
  fun onRemoved(position: Int, count: Int)
}

expect enum class DiffingChangePayload {
  ITEM_TO_PLACEHOLDER,
  PLACEHOLDER_TO_ITEM,
  PLACEHOLDER_POSITION_CHANGE,
}
