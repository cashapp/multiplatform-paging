package app.cash.paging.samples.reposearch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emitAll
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
      RepoSearchTheme {
        RepoSearchContent(
          viewModel = viewModel,
          onEvent = { event ->
            events.tryEmit(event)
          },
        )
      }
    }
  }
}
