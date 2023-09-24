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

@file:Suppress("NOTHING_TO_INLINE")

package app.cash.paging

import androidx.paging.filter
import androidx.paging.flatMap
import androidx.paging.insertFooterItem
import androidx.paging.insertHeaderItem
import androidx.paging.insertSeparators
import androidx.paging.map
import kotlin.jvm.JvmOverloads
import kotlin.jvm.JvmSynthetic

@JvmSynthetic
actual inline fun <T : Any, R : Any> PagingData<T>.map(
  noinline transform: suspend (T) -> R,
): PagingData<R> = map(transform)

@JvmSynthetic
actual inline fun <T : Any, R : Any> PagingData<T>.flatMap(
  noinline transform: suspend (T) -> Iterable<R>,
): PagingData<R> = flatMap(transform)

@JvmSynthetic
actual inline fun <T : Any> PagingData<T>.filter(
  noinline predicate: suspend (T) -> Boolean,
): PagingData<T> = filter(predicate)

@JvmSynthetic
actual inline fun <T : R, R : Any> PagingData<T>.insertSeparators(
  terminalSeparatorType: TerminalSeparatorType,
  noinline generator: suspend (T?, T?) -> R?,
): PagingData<R> = insertSeparators(terminalSeparatorType, generator)

@JvmOverloads
actual inline fun <T : Any> PagingData<T>.insertHeaderItem(
  terminalSeparatorType: TerminalSeparatorType,
  item: T,
): PagingData<T> = insertHeaderItem(terminalSeparatorType, item)

@JvmOverloads
actual inline fun <T : Any> PagingData<T>.insertFooterItem(
  terminalSeparatorType: TerminalSeparatorType,
  item: T,
): PagingData<T> = insertFooterItem(terminalSeparatorType, item)
