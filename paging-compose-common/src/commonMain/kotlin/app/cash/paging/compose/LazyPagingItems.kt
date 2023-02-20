/*
 * Copyright 2020 The Android Open Source Project
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

package app.cash.paging.compose

import androidx.compose.runtime.Composable
import app.cash.paging.CombinedLoadStates
import app.cash.paging.ItemSnapshotList
import app.cash.paging.PagingData
import kotlinx.coroutines.flow.Flow
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

expect class LazyPagingItems<T : Any> {
  var itemSnapshotList: ItemSnapshotList<T>
    private set

  val itemCount: Int

  operator fun get(index: Int): T?

  fun peek(index: Int): T?

  fun retry()

  fun refresh()

  var loadState: CombinedLoadStates
    private set
}

@Composable
expect fun <T : Any> Flow<PagingData<T>>.collectAsLazyPagingItems(
  context: CoroutineContext,
): LazyPagingItems<T>

// https://issuetracker.google.com/issues/196413692
@Composable
fun <T : Any> Flow<PagingData<T>>.collectAsLazyPagingItems(): LazyPagingItems<T> =
  collectAsLazyPagingItems(EmptyCoroutineContext)
