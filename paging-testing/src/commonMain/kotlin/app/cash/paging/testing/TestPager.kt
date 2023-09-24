/*
 * Copyright 2022 The Android Open Source Project
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

package app.cash.paging.testing

import app.cash.paging.PagingConfig
import app.cash.paging.PagingSource
import app.cash.paging.PagingSourceLoadResult
import app.cash.paging.PagingSourceLoadResultPage
import app.cash.paging.PagingState
import kotlin.jvm.JvmSuppressWildcards

expect class TestPager<Key : Any, Value : Any>(
  config: PagingConfig,
  pagingSource: PagingSource<Key, Value>,
) {
  suspend fun refresh(
    initialKey: Key? /* = null */,
  ): @JvmSuppressWildcards PagingSourceLoadResult<Key, Value>

  suspend fun append(): @JvmSuppressWildcards PagingSourceLoadResult<Key, Value>?

  suspend fun prepend(): @JvmSuppressWildcards PagingSourceLoadResult<Key, Value>?

  suspend fun getLastLoadedPage(): @JvmSuppressWildcards PagingSourceLoadResultPage<Key, Value>?

  suspend fun getPages(): @JvmSuppressWildcards List<PagingSourceLoadResultPage<Key, Value>>

  suspend fun getPagingState(
    anchorPosition: Int,
  ): @JvmSuppressWildcards PagingState<Key, Value>

  suspend fun getPagingState(
    anchorPositionLookup: (item: @JvmSuppressWildcards Value) -> Boolean,
  ): @JvmSuppressWildcards PagingState<Key, Value>
}
