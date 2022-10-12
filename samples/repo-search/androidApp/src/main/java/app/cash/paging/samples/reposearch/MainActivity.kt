package app.cash.paging.samples.reposearch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
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
      RepoSearchTheme {
        RepoSearch(viewModel) { event ->
          events.tryEmit(event)
        }
      }
    }
  }
}

@Composable
fun RepoSearch(
  viewModel: ViewModel,
  events: (Event) -> Unit,
) {
  Surface(Modifier.fillMaxSize()) {
    Column {
      when (viewModel) {
        is ViewModel.Empty -> {
          SearchField("", emptyFlow<PagingData<Repository>>().collectAsLazyPagingItems(), events)
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

@Composable
fun SearchField(
  searchTerm: String,
  repositories: LazyPagingItems<Repository>,
  events: (Event) -> Unit,
) {
  var textFieldValue by remember { mutableStateOf(TextFieldValue(searchTerm)) }
  TextField(
    textFieldValue,
    onValueChange = { textFieldValue = it },
    Modifier
      .fillMaxWidth()
      .padding(start = 16.dp, top = 16.dp, end = 16.dp),
    placeholder = { Text("Search for a repository…") },
    keyboardOptions = KeyboardOptions(autoCorrect = false, imeAction = ImeAction.Search),
    keyboardActions = KeyboardActions(
      onSearch = {
        events(Event.SearchTerm(textFieldValue.text))
        repositories.refresh()
      }
    ),
    singleLine = true,
  )
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
