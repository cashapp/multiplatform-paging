package app.cash.paging

import androidx.paging.PagingSource

// TODO Why did we need PagingSourceFactory in the first place? Was it to do with not being able to do something with Kotlin/Js? Can we just use Function0 in JS?
//actual interface PagingSourceFactory<Key : Any, Value : Any> : Function0<PagingSource<Key, Value>>
