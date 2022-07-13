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

// TODO We should be able to expect/actual the sealed class as a typealias as expected, but we're blocked until
//  https://youtrack.jetbrains.com/issue/KT-34281 is fixed.

expect sealed class LoadState(
  endOfPaginationReached: Boolean
) {
  val endOfPaginationReached: Boolean
}

expect class LoadStateNotLoading(endOfPaginationReached: Boolean)
expect object LoadStateLoading
expect class LoadStateError(error: Throwable) {
  val error: Throwable
}
