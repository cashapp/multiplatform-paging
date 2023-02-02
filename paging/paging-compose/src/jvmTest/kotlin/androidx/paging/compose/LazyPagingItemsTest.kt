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

package androidx.paging.compose

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.TestPagingSource
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.isActive
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertFalse
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LazyPagingItemsTest {
    @get:Rule
    val rule = createComposeRule()

    val items = (1..10).toList().map { it }

    private fun createPager(
        config: PagingConfig = PagingConfig(
            pageSize = 1,
            enablePlaceholders = false,
            maxSize = 200,
            initialLoadSize = 3,
            prefetchDistance = 1,
        ),
        loadDelay: Long = 0,
        pagingSourceFactory: () -> PagingSource<Int, Int> = {
            TestPagingSource(items = items, loadDelay = loadDelay)
        }
    ): Pager<Int, Int> {
        return Pager(config = config, pagingSourceFactory = pagingSourceFactory)
    }

    private fun createPagerWithPlaceholders(
        config: PagingConfig = PagingConfig(
            pageSize = 1,
            enablePlaceholders = true,
            maxSize = 200,
            initialLoadSize = 3,
            prefetchDistance = 0,
        )
    ) = Pager(
        config = config,
        pagingSourceFactory = { TestPagingSource(items = items, loadDelay = 0) }
    )

    @Before
    fun before() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun after() {
        Dispatchers.resetMain()
    }

    @Test
    fun lazyPagingInitialLoadState() {
        val pager = createPager()
        val loadStates: MutableList<CombinedLoadStates> = mutableListOf()
        rule.setContent {
            val lazyPagingItems = pager.flow.collectAsLazyPagingItems()
            loadStates.add(lazyPagingItems.loadState)
        }

        rule.waitForIdle()

        val expected = CombinedLoadStates(
            refresh = LoadState.Loading,
            prepend = LoadState.NotLoading(false),
            append = LoadState.NotLoading(false),
            source = LoadStates(
                LoadState.Loading,
                LoadState.NotLoading(false),
                LoadState.NotLoading(false)
            ),
            mediator = null
        )
        assertThat(loadStates).isNotEmpty()
        assertThat(loadStates.first()).isEqualTo(expected)
    }

    @Test
    fun lazyPagingLoadStateAfterRefresh() {
        val pager = createPager(loadDelay = 100)

        val loadStates: MutableList<CombinedLoadStates> = mutableListOf()

        lateinit var lazyPagingItems: LazyPagingItems<Int>
        rule.setContent {
            lazyPagingItems = pager.flow.collectAsLazyPagingItems()
            loadStates.add(lazyPagingItems.loadState)
        }

        // wait for both compose and paging to complete
        rule.waitUntil {
            rule.waitForIdle()
            lazyPagingItems.loadState.refresh == LoadState.NotLoading(false)
        }

        rule.runOnIdle {
            // we only want loadStates after manual refresh
            loadStates.clear()
            lazyPagingItems.refresh()
        }

        // wait for both compose and paging to complete
        rule.waitUntil {
            rule.waitForIdle()
            lazyPagingItems.loadState.refresh == LoadState.NotLoading(false)
        }

        assertThat(loadStates).isNotEmpty()
        val expected = CombinedLoadStates(
            refresh = LoadState.Loading,
            prepend = LoadState.NotLoading(false),
            append = LoadState.NotLoading(false),
            source = LoadStates(
                LoadState.Loading,
                LoadState.NotLoading(false),
                LoadState.NotLoading(false)
            ),
            mediator = null
        )
        assertThat(loadStates.first()).isEqualTo(expected)
    }

    @Test
    fun itemSnapshotList() {
        lateinit var lazyPagingItems: LazyPagingItems<Int>
        val pager = createPager()
        var composedCount = 0
        rule.setContent {
            lazyPagingItems = pager.flow.collectAsLazyPagingItems()

            for (i in 0 until lazyPagingItems.itemCount) {
                lazyPagingItems[i]
            }
            composedCount = lazyPagingItems.itemCount
        }

        rule.waitUntil {
            composedCount == items.size
        }

        assertThat(lazyPagingItems.itemSnapshotList).isEqualTo(items)
    }

    @Test
    fun peek() {
        lateinit var lazyPagingItems: LazyPagingItems<Int>
        var composedCount = 0
        val pager = createPager()
        rule.setContent {
            lazyPagingItems = pager.flow.collectAsLazyPagingItems()

            // Trigger page fetch until all items 0-6 are loaded
            for (i in 0 until minOf(lazyPagingItems.itemCount, 5)) {
                lazyPagingItems[i]
            }
            composedCount = lazyPagingItems.itemCount
        }

        rule.waitUntil {
            composedCount == 6
        }

        rule.runOnIdle {
            assertThat(lazyPagingItems.itemCount).isEqualTo(6)
            for (i in 0..4) {
                assertThat(lazyPagingItems.peek(i)).isEqualTo(items[i])
            }
        }

        rule.runOnIdle {
            // Verify peek does not trigger page fetch.
            assertThat(lazyPagingItems.itemCount).isEqualTo(6)
        }
    }

    @Test
    fun retry() {
        var factoryCallCount = 0
        lateinit var pagingSource: TestPagingSource
        val pager = createPager {
            factoryCallCount++
            pagingSource = TestPagingSource(items = items, loadDelay = 0)
            if (factoryCallCount == 1) {
                pagingSource.errorNextLoad = true
            }
            pagingSource
        }

        lateinit var lazyPagingItems: LazyPagingItems<Int>
        rule.setContent {
            lazyPagingItems = pager.flow.collectAsLazyPagingItems()
        }

        assertThat(lazyPagingItems.itemSnapshotList).isEmpty()

        lazyPagingItems.retry()
        rule.waitForIdle()

        assertThat(lazyPagingItems.itemSnapshotList).isNotEmpty()
        // Verify retry does not invalidate.
        assertThat(factoryCallCount).isEqualTo(1)
    }

    @Test
    fun refresh() {
        var factoryCallCount = 0
        lateinit var pagingSource: TestPagingSource
        val pager = createPager {
            factoryCallCount++
            pagingSource = TestPagingSource(items = items, loadDelay = 0)
            if (factoryCallCount == 1) {
                pagingSource.errorNextLoad = true
            }
            pagingSource
        }

        lateinit var lazyPagingItems: LazyPagingItems<Int>
        rule.setContent {
            lazyPagingItems = pager.flow.collectAsLazyPagingItems()
        }

        assertThat(lazyPagingItems.itemSnapshotList).isEmpty()

        lazyPagingItems.refresh()
        rule.waitForIdle()

        assertThat(lazyPagingItems.itemSnapshotList).isNotEmpty()
        assertThat(factoryCallCount).isEqualTo(2)
    }

    @Test
    fun itemCountIsObservable() {
        val items = mutableListOf(0, 1)
        val pager = createPager {
            TestPagingSource(items = items, loadDelay = 0)
        }

        var composedCount = 0
        lateinit var lazyPagingItems: LazyPagingItems<Int>
        rule.setContent {
            lazyPagingItems = pager.flow.collectAsLazyPagingItems()
            composedCount = lazyPagingItems.itemCount
        }

        rule.waitUntil {
            composedCount == 2
        }

        rule.runOnIdle {
            items += 2
            lazyPagingItems.refresh()
        }

        rule.waitUntil {
            composedCount == 3
        }

        rule.runOnIdle {
            items.clear()
            items.add(0)
            lazyPagingItems.refresh()
        }

        rule.waitUntil {
            composedCount == 1
        }
    }

    @Test
    fun collectOnDefaultThread() {
        val items = mutableListOf(1, 2, 3)
        val pager = createPager {
            TestPagingSource(items = items, loadDelay = 0)
        }

        lateinit var lazyPagingItems: LazyPagingItems<Int>
        rule.setContent {
            lazyPagingItems = pager.flow.collectAsLazyPagingItems()
        }

        rule.waitUntil {
            lazyPagingItems.itemCount == 3
        }
        assertThat(lazyPagingItems.itemSnapshotList).containsExactlyElementsIn(
            items
        )
    }

    @Test
    fun collectOnWorkerThread() {
        val items = mutableListOf(1, 2, 3)
        val pager = createPager {
            TestPagingSource(items = items, loadDelay = 0)
        }

        val context = StandardTestDispatcher()
        lateinit var lazyPagingItems: LazyPagingItems<Int>
        rule.setContent {
            lazyPagingItems = pager.flow.collectAsLazyPagingItems(context)
        }

        rule.runOnIdle {
            assertFalse(context.isActive)
            // collection should not have started yet
            assertThat(lazyPagingItems.itemSnapshotList).isEmpty()
        }

        // start LaunchedEffects
        context.scheduler.advanceUntilIdle()

        rule.runOnIdle {
            // continue with pagingDataDiffer collections
            context.scheduler.advanceUntilIdle()
        }
        rule.waitUntil {
            lazyPagingItems.itemCount == items.size
        }
        assertThat(lazyPagingItems.itemSnapshotList).containsExactlyElementsIn(
            items
        )
    }
}
