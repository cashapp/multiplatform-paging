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

@file:JvmName("CommonPagingConfig") // TODO I have no idea why I need this one

package app.cash.paging

import androidx.paging.COUNT_UNDEFINED
import kotlin.jvm.JvmField
import kotlin.jvm.JvmName
import kotlin.jvm.JvmOverloads

expect class PagingConfig @JvmOverloads constructor(
  pageSize: Int,
  prefetchDistance: Int = pageSize,
  enablePlaceholders: Boolean = true,
  initialLoadSize: Int = pageSize * DEFAULT_INITIAL_PAGE_MULTIPLIER,
  maxSize: Int = MAX_SIZE_UNBOUNDED,
  jumpThreshold: Int = COUNT_UNDEFINED
) {

  @JvmField
  val pageSize: Int

  @JvmField
  val prefetchDistance: Int

  @JvmField
  val enablePlaceholders: Boolean

  @JvmField
  val initialLoadSize: Int

  @JvmField
  val maxSize: Int

  @JvmField
  val jumpThreshold: Int

  companion object
}

// TODO expect/actual const isn't supported yet. Moved to a top-level const for now.
//  https://youtrack.jetbrains.com/issue/KT-18856
const val MAX_SIZE_UNBOUNDED: Int = Int.MAX_VALUE

private const val DEFAULT_INITIAL_PAGE_MULTIPLIER = 3
