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

package androidx.appactions.interaction.capabilities.testing.internal

import androidx.appactions.interaction.capabilities.core.ActionExecutorAsync
import androidx.appactions.interaction.capabilities.core.ExecutionResult
import androidx.appactions.interaction.capabilities.core.impl.concurrent.Futures

object TestingUtils {
    // use this timeout for things that should take negligible time.
    const val CB_TIMEOUT = 1000L
    // use this timeout for waiting an arbitrary period of time.
    const val BLOCKING_TIMEOUT = 300L

    fun <ArgumentT, OutputT> createFakeActionExecutor(): ActionExecutorAsync<ArgumentT, OutputT> {
        return ActionExecutorAsync { _: ArgumentT ->
            Futures.immediateFuture(
                ExecutionResult.getDefaultInstance(),
            )
        }
    }
}
