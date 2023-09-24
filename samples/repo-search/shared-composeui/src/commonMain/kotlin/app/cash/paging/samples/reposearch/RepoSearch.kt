package app.cash.paging.samples.reposearch

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.PagingData
import app.cash.paging.LoadStateError
import app.cash.paging.LoadStateLoading
import app.cash.paging.LoadStateNotLoading
import app.cash.paging.compose.LazyPagingItems
import app.cash.paging.compose.collectAsLazyPagingItems
import kotlinx.coroutines.flow.emptyFlow

@Composable
private fun RepoSearchEmpty(
  events: (Event) -> Unit,
  onRefreshList: () -> Unit,
) {
  Surface(Modifier.fillMaxSize()) {
    Column {
      SearchField(
        searchTerm = "",
        events = events,
        onRefreshList = onRefreshList,
      )
    }
  }
}

@Composable
private fun RepoSearchResults(
  searchTerm: String,
  events: (Event) -> Unit,
  searchResults: @Composable ColumnScope.() -> Unit,
  onRefreshList: () -> Unit,
) {
  Surface(Modifier.fillMaxSize()) {
    Column {
      SearchField(
        searchTerm = searchTerm,
        events = events,
        onRefreshList = onRefreshList,
      )
      searchResults()
    }
  }
}

@Composable
fun RepoSearchContent(
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
      LoadStateLoading -> {
        item {
          CircularProgressIndicator()
        }
      }
      is LoadStateNotLoading -> {
        items(repositories.itemCount) { index ->
          val repository = repositories[index]
          Row(Modifier.fillMaxWidth()) {
            Text(
              repository!!.fullName,
              Modifier.weight(1f),
            )
            Text(repository.stargazersCount.toString())
          }
        }
      }
      is LoadStateError -> {
        item {
          Text(loadState.error.message!!)
        }
      }
      else -> error("when should be exhaustive")
    }
  }
}
