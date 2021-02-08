/*
 * Copyright 2021 The Android Open Source Project
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

package androidx.compose.ui.inspection

import androidx.compose.ui.inspection.rules.JvmtiRule
import androidx.compose.ui.inspection.testdata.TestLambdas
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.google.common.truth.Truth.assertThat
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@SmallTest
@RunWith(AndroidJUnit4::class)
class LambdaLocationTest {

    @get:Rule
    val rule = JvmtiRule()

    @Test
    @Ignore // b/179040867
    fun test() {
        assertThat(LambdaLocation.resolve(TestLambdas.short))
            .isEqualTo(LambdaLocation("TestLambdas.kt", 20, 20))
        assertThat(LambdaLocation.resolve(TestLambdas.long))
            .isEqualTo(LambdaLocation("TestLambdas.kt", 22, 24))
    }
}