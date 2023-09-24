/*
 * Copyright 2022 The Android Open Source Project
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

package app.cash.paging.testing

import app.cash.paging.PagingData
import app.cash.paging.testing.ErrorRecovery.THROW
import kotlinx.coroutines.flow.Flow
import kotlin.jvm.JvmSuppressWildcards

expect suspend fun <Value : Any> Flow<PagingData<Value>>.asSnapshot(
  onError: LoadErrorHandler = LoadErrorHandler { THROW },
  loadOperations: suspend SnapshotLoader<Value>.() -> @JvmSuppressWildcards Unit = { },
): @JvmSuppressWildcards List<Value>
