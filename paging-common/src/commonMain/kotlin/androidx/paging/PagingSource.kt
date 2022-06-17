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

@file:JvmName("CommonPagingSource")

package androidx.paging

import app.cash.paging.PagingState
import kotlin.jvm.JvmName

expect abstract class CommonPagingSource<Key : Any, Value : Any> {

  open val jumpingSupported: Boolean

  open val keyReuseSupported: Boolean

  val invalid: Boolean

  fun invalidate()

  fun registerInvalidatedCallback(onInvalidatedCallback: () -> Unit)

  fun unregisterInvalidatedCallback(onInvalidatedCallback: () -> Unit)

  abstract suspend fun load(params: CommonPagingSourceLoadParams<Key>): CommonPagingSourceLoadResult<Key, Value>

  abstract fun getRefreshKey(state: PagingState<Key, Value>): Key?
}

expect sealed class CommonPagingSourceLoadParams<Key : Any> constructor(
  loadSize: Int,
  placeholdersEnabled: Boolean,
) {
  val loadSize: Int
  val placeholdersEnabled: Boolean
  abstract val key: Key?
}

// TODO Alec's solution to not have the sealed class be extended by it's subclasses might work with jvmMain (through
//  typealiases) but I'm not too sure how it's going to work on nonJvmMain. Could I just do typealiases there too???

expect class CommonPagingSourceLoadParamsRefresh<Key : Any> constructor(
  key: Key?,
  loadSize: Int,
  placeholdersEnabled: Boolean,
)

expect class CommonPagingSourceLoadParamsAppend<Key : Any> constructor(
  key: Key,
  loadSize: Int,
  placeholdersEnabled: Boolean,
)

expect class CommonPagingSourceLoadParamsPrepend<Key : Any> constructor(
  key: Key,
  loadSize: Int,
  placeholdersEnabled: Boolean,
)

expect sealed class CommonPagingSourceLoadResult<Key : Any, Value : Any>

expect class CommonPagingSourceLoadResultError<Key : Any, Value : Any>(
  throwable: Throwable
) {
  val throwable: Throwable
}

expect class CommonPagingSourceLoadResultInvalid<Key : Any, Value : Any>()

expect class CommonPagingSourceLoadResultPage<Key : Any, Value : Any> constructor(
  data: List<Value>,
  prevKey: Key?,
  nextKey: Key?,
  itemsBefore: Int = COUNT_UNDEFINED,
  itemsAfter: Int = COUNT_UNDEFINED
) {

  val data: List<Value>
  val prevKey: Key?
  val nextKey: Key?
  val itemsBefore: Int
  val itemsAfter: Int

  constructor(
    data: List<Value>,
    prevKey: Key?,
    nextKey: Key?
  )

  companion object
}

// TODO expect/actual const isn't supported yet. Moved to a top-level const for now.
//  https://youtrack.jetbrains.com/issue/KT-18856
const val COUNT_UNDEFINED: Int = Int.MIN_VALUE
