import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.paging.PagingData
import app.cash.paging.collectAsLazyPagingItems
import app.cash.paging.samples.reposearch.Event
import app.cash.paging.samples.reposearch.RepoSearchPresenter
import app.cash.paging.samples.reposearch.Repository
import app.cash.paging.samples.reposearch.ViewModel
import app.cash.paging.samples.reposearch.ui.RepoDemoTheme
import app.cash.paging.samples.reposearch.ui.RepoSearchEmpty
import app.cash.paging.samples.reposearch.ui.RepoSearchResults
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch

fun main() = application {
  Window(onCloseRequest = ::exitApplication) {
    App()
  }
}

@Composable
@Preview
fun App() {

  val events = MutableSharedFlow<Event>(extraBufferCapacity = Int.MAX_VALUE)
  val viewModels = MutableStateFlow<ViewModel>(ViewModel.Empty)
  val viewModel by viewModels.collectAsState()
  val presenter = RepoSearchPresenter()
  val scope = rememberCoroutineScope()

  scope.launch {
    viewModels.emitAll(presenter.produceViewModels(events))
  }

  RepoDemoTheme {
    RepoSearchContent(
      viewModel = viewModel,
      events = { event ->
        events.tryEmit(event)
      },
    )
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
