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

expect class LoadStates(
  refresh: LoadState,
  prepend: LoadState,
  append: LoadState,
) {
  val refresh: LoadState
  val prepend: LoadState
  val append: LoadState

  operator fun component1(): LoadState
  operator fun component2(): LoadState
  operator fun component3(): LoadState

  /** @suppress */
  inline fun forEach(op: (LoadType, LoadState) -> Unit)
}

fun LoadStates.copy(
  refresh: LoadState = this.refresh,
  prepend: LoadState = this.prepend,
  append: LoadState = this.append,
): LoadStates {
  return LoadStates(
    refresh,
    prepend,
    append,
  )
}
