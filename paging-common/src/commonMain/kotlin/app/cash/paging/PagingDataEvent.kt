/*
 * Copyright 2023 The Android Open Source Project
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

expect sealed class PagingDataEvent<T : Any>

expect class PagingDataEventPrepend<T : Any>(
  inserted: List<T>,
  newPlaceholdersBefore: Int,
  oldPlaceholdersBefore: Int,
) {
  val inserted: List<T>
  val newPlaceholdersBefore: Int
  val oldPlaceholdersBefore: Int
}

expect class PagingDataEventAppend<T : Any>(
  startIndex: Int,
  inserted: List<T>,
  newPlaceholdersAfter: Int,
  oldPlaceholdersAfter: Int,
) {
  val startIndex: Int
  val inserted: List<T>
  val newPlaceholdersAfter: Int
  val oldPlaceholdersAfter: Int
}

expect class PagingDataEventRefresh<T : Any>(
  newList: PlaceholderPaddedList<T>,
  previousList: PlaceholderPaddedList<T>,
) {
  val newList: PlaceholderPaddedList<T>
  val previousList: PlaceholderPaddedList<T>
}

expect class PagingDataEventDropPrepend<T : Any>(
  dropCount: Int,
  newPlaceholdersBefore: Int,
  oldPlaceholdersBefore: Int,
) {
  val dropCount: Int
  val newPlaceholdersBefore: Int
  val oldPlaceholdersBefore: Int
}

expect class PagingDataEventDropAppend<T : Any>(
  startIndex: Int,
  dropCount: Int,
  newPlaceholdersAfter: Int,
  oldPlaceholdersAfter: Int,
) {
  val startIndex: Int
  val dropCount: Int
  val newPlaceholdersAfter: Int
  val oldPlaceholdersAfter: Int
}
