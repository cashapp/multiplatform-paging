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

import kotlin.jvm.JvmOverloads
import kotlin.jvm.JvmStatic

expect class PagingData<T : Any> {
  companion object {

    @JvmStatic
    fun <T : Any> empty(): PagingData<T>

    @JvmOverloads
    @JvmStatic
    fun <T : Any> empty(
      sourceLoadStates: LoadStates,
      mediatorLoadStates: LoadStates?, /* = null */
    ): PagingData<T>

    @JvmStatic
    fun <T : Any> from(
      data: List<T>,
    ): PagingData<T>

    @JvmOverloads
    @JvmStatic
    fun <T : Any> from(
      data: List<T>,
      sourceLoadStates: LoadStates,
      mediatorLoadStates: LoadStates?, /* = null */
    ): PagingData<T>
  }
}
