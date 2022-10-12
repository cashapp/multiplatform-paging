package app.cash.paging.samples.reposearch

import app.cash.paging.PagingData
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

sealed interface Event {

  data class SearchTerm(
    val searchTerm: String,
  ) : Event
}

sealed interface ViewModel {

  object Empty : ViewModel

  data class SearchResults(
    val searchTerm: String,
    val repositories: Flow<PagingData<Repository>>,
  ) : ViewModel
}

@Serializable
data class Repositories(
  @SerialName("total_count") val totalCount: Int,
  val items: List<Repository>,
)

@Serializable
data class Repository(
  @SerialName("full_name") val fullName: String,
  @SerialName("stargazers_count") val stargazersCount: Int,
)

object RateLimitExceeded : Exception()
