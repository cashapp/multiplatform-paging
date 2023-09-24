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

@file:JvmName("PagingDataTransforms")
@file:JvmMultifileClass

package app.cash.paging

import app.cash.paging.TerminalSeparatorType.FULLY_COMPLETE
import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import kotlin.jvm.JvmOverloads
import kotlin.jvm.JvmSynthetic

@JvmSynthetic
expect fun <T : Any, R : Any> PagingData<T>.map(
  transform: suspend (T) -> R,
): PagingData<R>

@JvmSynthetic
expect fun <T : Any, R : Any> PagingData<T>.flatMap(
  transform: suspend (T) -> Iterable<R>,
): PagingData<R>

@JvmSynthetic
expect fun <T : Any> PagingData<T>.filter(
  predicate: suspend (T) -> Boolean,
): PagingData<T>

@JvmSynthetic
expect fun <T : R, R : Any> PagingData<T>.insertSeparators(
  terminalSeparatorType: TerminalSeparatorType = FULLY_COMPLETE,
  generator: suspend (T?, T?) -> R?,
): PagingData<R>

@JvmOverloads
expect fun <T : Any> PagingData<T>.insertHeaderItem(
  terminalSeparatorType: TerminalSeparatorType = FULLY_COMPLETE,
  item: T,
): PagingData<T>

@JvmOverloads
expect fun <T : Any> PagingData<T>.insertFooterItem(
  terminalSeparatorType: TerminalSeparatorType = FULLY_COMPLETE,
  item: T,
): PagingData<T>
