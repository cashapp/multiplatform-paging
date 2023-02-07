package app.cash.paging.samples.reposearch

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun RepoSearchEmpty(
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
fun RepoSearchResults(
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
