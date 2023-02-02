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

package androidx.paging.compose

import android.util.Log
import androidx.paging.LOG_TAG
import androidx.paging.Logger

internal actual val platformLogger: Logger? = object : Logger {
    override fun isLoggable(level: Int): Boolean {
        return Log.isLoggable(LOG_TAG, level)
    }

    override fun log(level: Int, message: String, tr: Throwable?) {
        when {
            tr != null && level == Log.DEBUG -> Log.d(LOG_TAG, message, tr)
            tr != null && level == Log.VERBOSE -> Log.v(LOG_TAG, message, tr)
            level == Log.DEBUG -> Log.d(LOG_TAG, message)
            level == Log.VERBOSE -> Log.v(LOG_TAG, message)
            else -> {
                throw IllegalArgumentException(
                    "debug level $level is requested but Paging only supports " +
                        "default logging for level 2 (DEBUG) or level 3 (VERBOSE)"
                )
            }
        }
    }
}
