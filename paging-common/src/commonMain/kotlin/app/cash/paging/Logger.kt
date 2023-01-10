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

@file:JvmName("Logger")

package app.cash.paging

import kotlin.jvm.JvmName

expect var LOGGER: Logger?

const val LOG_TAG: String = "Paging"

/**
 * @hide
 */
expect interface Logger {
  fun isLoggable(level: Int): Boolean
  fun log(level: Int, message: String, tr: Throwable? = null)
}

/**
 * @hide
 */
expect inline fun log(
  level: Int,
  tr: Throwable? = null,
  block: () -> String,
)

/**
 * @hide
 */
const val VERBOSE: Int = 2

/**
 * @hide
 */
const val DEBUG: Int = 3
