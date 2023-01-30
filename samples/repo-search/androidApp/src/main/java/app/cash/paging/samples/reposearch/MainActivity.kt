package app.cash.paging.samples.reposearch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.paging.PagingData
import app.cash.paging.LazyPagingItems
import app.cash.paging.collectAsLazyPagingItems
import app.cash.paging.items
import app.cash.paging.samples.reposearch.ui.RepoDemoTheme
import app.cash.paging.samples.reposearch.ui.RepoSearchEmpty
import app.cash.paging.samples.reposearch.ui.RepoSearchResults
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val events = MutableSharedFlow<Event>(extraBufferCapacity = Int.MAX_VALUE)
    val viewModels = MutableStateFlow<ViewModel>(ViewModel.Empty)

    val presenter = RepoSearchPresenter()

    lifecycleScope.launch {
      viewModels.emitAll(presenter.produceViewModels(events))
    }

    setContent {
      val viewModel by viewModels.collectAsState()
      RepoDemoTheme {
        RepoSearchContent(
          viewModel = viewModel,
          events = { event ->
            events.tryEmit(event)
          },
        )
      }
    }
  }
}

@Composable
private fun RepoSearchContent(
  viewModel: ViewModel,
  events: (Event) -> Unit,
) {
  when (viewModel) {
    ViewModel.Empty -> {
      val repositories = emptyFlow<PagingData<Repository>>().collectAsLazyPagingItems()
      RepoSearchEmpty(
        events = events,
        onRefreshList = repositories::refresh,
      )
    }

    is ViewModel.SearchResults -> {
      val repositories = viewModel.repositories.collectAsLazyPagingItems()
      RepoSearchResults(
        searchTerm = viewModel.searchTerm,
        events = events,
        searchResults = { SearchResults(repositories) },
        onRefreshList = repositories::refresh,
      )
    }
  }
}

@Composable
private fun SearchResults(repositories: LazyPagingItems<Repository>) {
  LazyColumn(
    Modifier.fillMaxWidth(),
    contentPadding = PaddingValues(16.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    when (val loadState = repositories.loadState.refresh) {
      LoadState.Loading -> {
        item {
          CircularProgressIndicator()
        }
      }
      is LoadState.NotLoading -> {
        items(repositories) { repository ->
          Row(Modifier.fillMaxWidth()) {
            Text(
              repository!!.fullName,
              Modifier.weight(1f),
            )
            Text(repository.stargazersCount.toString())
          }
        }
      }
      is LoadState.Error -> {
        item {
          Text(loadState.error.message!!)
        }
      }
    }
  }
}
