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

package androidx.compose.material3

import android.os.Build
import androidx.compose.testutils.assertAgainstGolden
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.filters.SdkSuppress
import androidx.test.screenshot.AndroidXScreenshotTestRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
@SdkSuppress(minSdkVersion = Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)

class SegmentedButtonScreenshotTest {

    @get:Rule
    val rule = createComposeRule()

    @get:Rule
    val screenshotRule = AndroidXScreenshotTestRule(GOLDEN_MATERIAL3)

    @Test
    fun all_unselected() {
        rule.setMaterialContent(lightColorScheme()) {
            SegmentedButtonRow(modifier = Modifier.testTag(testTag)) {
                values.forEach {
                    SegmentedButton(checked = false, onCheckedChange = {}) {
                        Text(it)
                    }
                }
            }
        }

        assertButtonAgainstGolden("all_unselected")
    }

    @Test
    fun all_selected() {
        rule.setMaterialContent(lightColorScheme()) {
            SegmentedButtonRow(modifier = Modifier.testTag(testTag)) {
                values.forEach {
                    SegmentedButton(checked = true, onCheckedChange = {}) {
                        Text(it)
                    }
                }
            }
        }

        assertButtonAgainstGolden("all_selected")
    }

    @Test
    fun middle_selected() {
        rule.setMaterialContent(lightColorScheme()) {
            SegmentedButtonRow(modifier = Modifier.testTag(testTag)) {
                values.forEachIndexed { index, item ->
                    SegmentedButton(checked = index == 1, onCheckedChange = {}) {
                        Text(item)
                    }
                }
            }
        }

        assertButtonAgainstGolden("middle_selected")
    }

    @Test
    fun stroke_zIndex() {
        rule.setMaterialContent(lightColorScheme()) {
            val colors = SegmentedButtonDefaults.colors(
                checkedBorderColor = Color.Blue,
                uncheckedBorderColor = Color.Yellow
            )
            SegmentedButtonRow(modifier = Modifier.testTag(testTag)) {
                values.forEachIndexed { index, item ->
                    SegmentedButton(
                        checked = index == 1,
                        onCheckedChange = {},
                        colors = colors
                    ) {
                        Text(item)
                    }
                }
            }
        }

        assertButtonAgainstGolden("stroke_zIndex")
    }

    @Test
    fun button_shape() {
        rule.setMaterialContent(lightColorScheme()) {
            val colors = SegmentedButtonDefaults.colors(
                checkedBorderColor = Color.Blue,
                uncheckedBorderColor = Color.Yellow
            )
            SegmentedButtonRow(modifier = Modifier.testTag(testTag)) {
                values.forEachIndexed { index, item ->
                    val shape = SegmentedButtonDefaults.shape(index, values.size)

                    SegmentedButton(
                        checked = index == 1,
                        onCheckedChange = {},
                        colors = colors,
                        shape = shape,
                    ) {
                        Text(item)
                    }
                }
            }
        }

        assertButtonAgainstGolden("button_shape")
    }

    @Test
    fun all_unselected_darkTheme() {
        rule.setMaterialContent(darkColorScheme()) {
            SegmentedButtonRow(modifier = Modifier.testTag(testTag)) {
                values.forEach {
                    SegmentedButton(checked = false, onCheckedChange = {}) {
                        Text(it)
                    }
                }
            }
        }

        assertButtonAgainstGolden("all_unselected_darkTheme")
    }

    @Test
    fun all_selected_darkTheme() {
        rule.setMaterialContent(darkColorScheme()) {
            SegmentedButtonRow(modifier = Modifier.testTag(testTag)) {
                values.forEach {
                    SegmentedButton(checked = true, onCheckedChange = {}) {
                        Text(it)
                    }
                }
            }
        }

        assertButtonAgainstGolden("all_selected_darkTheme")
    }

    private fun assertButtonAgainstGolden(goldenName: String) {
        rule.onNodeWithTag(testTag)
            .captureToImage()
            .assertAgainstGolden(screenshotRule, goldenName)
    }

    private val values = listOf("Day", "Month", "Week")

    private val testTag = "button"
}