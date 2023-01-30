import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import app.cash.paging.LazyPagingItems
import app.cash.paging.items
import app.cash.paging.samples.reposearch.Repository

@Composable
fun SearchResults(
  repositories: LazyPagingItems<Repository>
) {
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
