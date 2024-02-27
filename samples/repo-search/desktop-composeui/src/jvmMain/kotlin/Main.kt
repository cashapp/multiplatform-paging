import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import app.cash.paging.samples.reposearch.Event
import app.cash.paging.samples.reposearch.RepoSearchContent
import app.cash.paging.samples.reposearch.RepoSearchPresenter
import app.cash.paging.samples.reposearch.RepoSearchTheme
import app.cash.paging.samples.reposearch.ViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.launch

fun main() = application {
  Window(onCloseRequest = ::exitApplication) {
    App()
  }
}

@Composable
@Preview
private fun App() {
  val events = MutableSharedFlow<Event>(extraBufferCapacity = Int.MAX_VALUE)
  val viewModels = MutableStateFlow<ViewModel>(ViewModel.Empty)
  val viewModel by viewModels.collectAsState()
  val presenter = RepoSearchPresenter()
  val scope = rememberCoroutineScope()

  scope.launch {
    viewModels.emitAll(presenter.produceViewModels(events))
  }

  RepoSearchTheme {
    RepoSearchContent(
      viewModel = viewModel,
      onEvent = { event ->
        events.tryEmit(event)
      },
    )
  }
}
