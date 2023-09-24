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

expect abstract class PagingSource<Key : Any, Value : Any>() {

  open val jumpingSupported: Boolean

  open val keyReuseSupported: Boolean

  val invalid: Boolean

  fun invalidate()

  fun registerInvalidatedCallback(onInvalidatedCallback: () -> Unit)

  fun unregisterInvalidatedCallback(onInvalidatedCallback: () -> Unit)

  abstract suspend fun load(params: PagingSourceLoadParams<Key>): PagingSourceLoadResult<Key, Value>

  abstract fun getRefreshKey(state: PagingState<Key, Value>): Key?
}

expect sealed class PagingSourceLoadParams<Key : Any> constructor(
  loadSize: Int,
  placeholdersEnabled: Boolean,
) {
  val loadSize: Int
  val placeholdersEnabled: Boolean
  abstract val key: Key?
}

expect class PagingSourceLoadParamsRefresh<Key : Any> constructor(
  key: Key?,
  loadSize: Int,
  placeholdersEnabled: Boolean,
) {
  val loadSize: Int
  val placeholdersEnabled: Boolean
  val key: Key?
}

expect class PagingSourceLoadParamsAppend<Key : Any> constructor(
  key: Key,
  loadSize: Int,
  placeholdersEnabled: Boolean,
) {
  val loadSize: Int
  val placeholdersEnabled: Boolean
  val key: Key
}

expect class PagingSourceLoadParamsPrepend<Key : Any> constructor(
  key: Key,
  loadSize: Int,
  placeholdersEnabled: Boolean,
) {
  val loadSize: Int
  val placeholdersEnabled: Boolean
  val key: Key
}

expect sealed class PagingSourceLoadResult<Key : Any, Value : Any>

expect class PagingSourceLoadResultError<Key : Any, Value : Any>(
  throwable: Throwable,
) {
  val throwable: Throwable

  operator fun component1(): Throwable
}

fun <Key : Any, Value : Any> PagingSourceLoadResultError<Key, Value>.copy(
  throwable: Throwable = this.throwable,
): PagingSourceLoadResultError<Key, Value> {
  return PagingSourceLoadResultError(throwable)
}

expect class PagingSourceLoadResultInvalid<Key : Any, Value : Any>()

expect class PagingSourceLoadResultPage<Key : Any, Value : Any> constructor(
  data: List<Value>,
  prevKey: Key?,
  nextKey: Key?,
  itemsBefore: Int, /* = COUNT_UNDEFINED */
  itemsAfter: Int, /* = COUNT_UNDEFINED */
) : Iterable<Value> {
  val data: List<Value>
  val prevKey: Key?
  val nextKey: Key?
  val itemsBefore: Int
  val itemsAfter: Int

  constructor(
    data: List<Value>,
    prevKey: Key?,
    nextKey: Key?,
  )

  operator fun component1(): List<Value>
  operator fun component2(): Key?
  operator fun component3(): Key?
  operator fun component4(): Int
  operator fun component5(): Int

  companion object
}

fun <Key : Any, Value : Any> createPagingSourceLoadResultPage(
  data: List<Value>,
  prevKey: Key?,
  nextKey: Key?,
  itemsBefore: Int = COUNT_UNDEFINED,
  itemsAfter: Int = COUNT_UNDEFINED,
): PagingSourceLoadResultPage<Key, Value> {
  return PagingSourceLoadResultPage(
    data,
    prevKey,
    nextKey,
    itemsBefore,
    itemsAfter,
  )
}

fun <Key : Any, Value : Any> PagingSourceLoadResultPage<Key, Value>.copy(
  data: List<Value> = this.data,
  prevKey: Key? = this.prevKey,
  nextKey: Key? = this.nextKey,
  itemsBefore: Int = this.itemsBefore,
  itemsAfter: Int = this.itemsAfter,
): PagingSourceLoadResultPage<Key, Value> {
  return PagingSourceLoadResultPage(
    data,
    prevKey,
    nextKey,
    itemsBefore,
    itemsAfter,
  )
}

const val COUNT_UNDEFINED: Int = Int.MIN_VALUE
