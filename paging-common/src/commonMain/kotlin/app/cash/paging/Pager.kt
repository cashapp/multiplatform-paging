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

package app.cash.paging

import kotlinx.coroutines.flow.Flow

expect class Pager<Key : Any, Value : Any> @ExperimentalPagingApi constructor(
  config: PagingConfig,
  initialKey: Key?, /* = null */
  remoteMediator: RemoteMediator<Key, Value>?,
  pagingSourceFactory: () -> PagingSource<Key, Value>,
) {
  // @JvmOverloads
  constructor(
    config: PagingConfig,
    initialKey: Key?, /* = null */
    pagingSourceFactory: () -> PagingSource<Key, Value>,
  )

  val flow: Flow<PagingData<Value>>
}

@ExperimentalPagingApi
fun <Key : Any, Value : Any> createPager(
  config: PagingConfig,
  initialKey: Key? = null,
  remoteMediator: RemoteMediator<Key, Value>?,
  pagingSourceFactory: () -> PagingSource<Key, Value>,
): Pager<Key, Value> {
  return Pager(
    config,
    initialKey,
    remoteMediator,
    pagingSourceFactory,
  )
}

fun <Key : Any, Value : Any> createPager(
  config: PagingConfig,
  initialKey: Key? = null,
  pagingSourceFactory: () -> PagingSource<Key, Value>,
): Pager<Key, Value> {
  return Pager(
    config,
    initialKey,
    pagingSourceFactory,
  )
}
