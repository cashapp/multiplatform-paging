import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import app.cash.paging.LazyPagingItems
import app.cash.paging.samples.reposearch.Event
import app.cash.paging.samples.reposearch.Repository

@Composable
fun SearchField(
  searchTerm: String,
  repositories: LazyPagingItems<Repository>,
  events: (Event) -> Unit,
) {
  var textFieldValue by remember { mutableStateOf(TextFieldValue(searchTerm)) }
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .wrapContentHeight()
      .padding(24.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {

    TextField(
      textFieldValue,
      onValueChange = { textFieldValue = it },
      Modifier
        .wrapContentHeight()
        .weight(1f)
        .padding(start = 16.dp, end = 16.dp),
      placeholder = { Text("Search for a repository…") },
      singleLine = true,
    )

    IconButton(
      onClick = {
        events(Event.SearchTerm(textFieldValue.text))
        repositories.refresh()
      }
    ) {
      Icon(imageVector = Icons.Default.Search, contentDescription = null)
    }
  }
}
