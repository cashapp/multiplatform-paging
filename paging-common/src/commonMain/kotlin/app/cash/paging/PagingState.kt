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

expect class PagingState<Key : Any, Value : Any> constructor(
  pages: List<PagingSourceLoadResultPage<Key, Value>>,
  anchorPosition: Int?,
  config: PagingConfig,
  leadingPlaceholderCount: Int,
) {

  val pages: List<PagingSourceLoadResultPage<Key, Value>>
  val anchorPosition: Int?
  val config: PagingConfig

  fun closestItemToPosition(anchorPosition: Int): Value?

  fun closestPageToPosition(anchorPosition: Int): PagingSourceLoadResultPage<Key, Value>?

  fun isEmpty(): Boolean

  fun firstItemOrNull(): Value?

  fun lastItemOrNull(): Value?
}
