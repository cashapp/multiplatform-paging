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
import kotlin.jvm.JvmSuppressWildcards

/** @suppress */
expect abstract class PagingDataPresenter<T : Any>(
  /* default = Dispatchers.Main */
  mainContext: CoroutineContext,
  /* default = null */
  cachedPagingData: PagingData<T>?,
) {

  abstract suspend fun presentPagingDataEvent(
    event: PagingDataEvent<T>,
  ): @JvmSuppressWildcards Unit

  suspend fun collectFrom(pagingData: PagingData<T>)

  val loadStateFlow: StateFlow<CombinedLoadStates?>

  val onPagesUpdatedFlow: Flow<Unit>

  fun addOnPagesUpdatedListener(listener: () -> Unit)

  fun removeOnPagesUpdatedListener(listener: () -> Unit)

  fun addLoadStateListener(listener: (@JvmSuppressWildcards CombinedLoadStates) -> Unit)

  fun removeLoadStateListener(listener: (@JvmSuppressWildcards CombinedLoadStates) -> Unit)
}

expect enum class DiffingChangePayload {
  ITEM_TO_PLACEHOLDER,
  PLACEHOLDER_TO_ITEM,
  PLACEHOLDER_POSITION_CHANGE,
}
