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

package androidx.paging

import androidx.recyclerview.widget.DiffUtil
import kotlin.coroutines.ContinuationInterceptor
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
/**
 * Test that repeatedly accessing edge items in paging will make it load all of the page even when
 * there is heavy filtering involved.
 */
class LoadFullListTest {

    private val testScope = TestScope()

    @BeforeTest
    fun before() {
        Dispatchers.setMain(
            testScope.coroutineContext[ContinuationInterceptor] as CoroutineDispatcher,
        )
    }

    @AfterTest
    fun after() {
        Dispatchers.resetMain()
    }

    @Test
    fun noFilter() = loadAll {
        true
    }

    @Test
    fun everyOtherItem() = loadAll {
        it % 2 == 0
    }

    @Test
    fun only1Item_firstPage() = loadAll {
        it == 2
    }

    @Test
    fun only1Item_lastPage() = loadAll(
        sourceSize = 30
    ) {
        it == 29
    }

    @Test
    fun noItems() = loadAll {
        false
    }

    @Test
    fun firstItemInEachPage() = loadAll {
        it % pageConfig.pageSize == 0
    }

    @Test
    fun firstItemInEvenPages() = loadAll {
        it % (pageConfig.pageSize * 2) == 0
    }

    @Test
    fun firstItemInOddPages() = loadAll {
        it % (pageConfig.pageSize * 2) == pageConfig.pageSize
    }

    @Test
    fun endOfPrefetchDistance() = loadAll {
        it % (pageConfig.prefetchDistance * 2) == pageConfig.prefetchDistance - 1
    }

    @Test
    fun rightAfterPrefetchDistance() = loadAll {
        it % (pageConfig.prefetchDistance * 2) == pageConfig.prefetchDistance
    }

    private fun loadAll(
        sourceSize: Int = 100,
        testFilter: (Int) -> Boolean
    ) = testScope.runTest {
        params().forEach { reverse ->
            val differ = AsyncPagingDataDiffer(
                diffCallback = object : DiffUtil.ItemCallback<Int>() {
                    override fun areContentsTheSame(oldItem: Int, newItem: Int): Boolean {
                        return oldItem == newItem
                    }

                    override fun areItemsTheSame(oldItem: Int, newItem: Int): Boolean {
                        return oldItem == newItem
                    }
                },
                updateCallback = ListUpdateCallbackFake(),
                workerDispatcher = Dispatchers.Main
            )

            val expectedFinalSize = (0 until sourceSize).count(testFilter)
            val pager = Pager(
                config = pageConfig,
                initialKey = if (reverse) {
                    sourceSize - 1
                } else {
                    0
                }
            ) {
                TestPagingSource(
                    items = List(sourceSize) { it }
                )
            }

            val job = launch {
                pager.flow.map { pagingData ->
                    pagingData.filter {
                        testFilter(it)
                    }
                }.collectLatest {
                    differ.submitData(it)
                }
            }

            advanceUntilIdle()
            // repeatedly load pages until all of the list is loaded
            while (differ.itemCount < expectedFinalSize) {
                val startSize = differ.itemCount
                if (reverse) {
                    differ.getItem(0)
                } else {
                    differ.getItem(differ.itemCount - 1)
                }
                advanceUntilIdle()
                if (differ.itemCount == startSize) {
                    break
                }
            }
            assertEquals(expectedFinalSize, differ.itemCount)

            job.cancel()
        }
    }

    companion object {
        fun params() = listOf(false, true)
        private val pageConfig = PagingConfig(
            pageSize = 5,
            prefetchDistance = 5,
            enablePlaceholders = false,
            initialLoadSize = 5
        )
    }
}
