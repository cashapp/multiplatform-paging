import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.paging.PagingData
import app.cash.paging.collectAsLazyPagingItems
import app.cash.paging.samples.reposearch.Event
import app.cash.paging.samples.reposearch.Repository
import app.cash.paging.samples.reposearch.ViewModel
import kotlinx.coroutines.flow.emptyFlow

@Composable
fun RepoSearch(
  viewModel: ViewModel,
  events: (Event) -> Unit,
) {
  Surface(Modifier.fillMaxSize()) {
    Column {
      when (viewModel) {
        is ViewModel.Empty -> {
          SearchField(
            "",
            emptyFlow<PagingData<Repository>>().collectAsLazyPagingItems(),
            events
          )
        }
        is ViewModel.SearchResults -> {
          val repositories = viewModel.repositories.collectAsLazyPagingItems()
          SearchField(viewModel.searchTerm, repositories, events)
          SearchResults(repositories)
        }
      }
    }
  }
}
