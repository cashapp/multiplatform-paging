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

package androidx.paging

actual typealias CommonPagingSource<Key, Value> = androidx.paging.PagingSource<Key, Value>

actual typealias CommonPagingSourceLoadParams<Key> = androidx.paging.PagingSource.LoadParams<Key>

actual typealias CommonPagingSourceLoadParamsRefresh<Key> = androidx.paging.PagingSource.LoadParams.Refresh<Key>
actual typealias CommonPagingSourceLoadParamsAppend<Key> = androidx.paging.PagingSource.LoadParams.Append<Key>
actual typealias CommonPagingSourceLoadParamsPrepend<Key> = androidx.paging.PagingSource.LoadParams.Prepend<Key>

actual typealias CommonPagingSourceLoadResult<Key, Value> = androidx.paging.PagingSource.LoadResult<Key, Value>

// Conflicts with the other actual typealias Error so just prefixed the surrounding class
actual typealias CommonPagingSourceLoadResultError<Key, Value> = androidx.paging.PagingSource.LoadResult.Error<Key, Value>
actual typealias CommonPagingSourceLoadResultInvalid<Key, Value> = androidx.paging.PagingSource.LoadResult.Invalid<Key, Value>
actual typealias CommonPagingSourceLoadResultPage<Key, Value> = androidx.paging.PagingSource.LoadResult.Page<Key, Value>
