# `paging-common`

## API discrepancies

A core goal of `app.cash.paging:paging-common` is to have an identical API to that of `androidx.paging:paging-common`.
Unfortunately, there are a few API discrepancies with `androidx.paging:paging-common` due to limitations in the Kotlin compiler.

> [!NOTE]
> Voting on the respective YouTrack issues may improve the likelihood of them being implemented.
> This could reduce the number of API discrepancies in future releases.

### Nested classes have been made top-level classes

#### Issue reference

https://youtrack.jetbrains.com/issue/KT-34281

#### Workaround

Use the newly defined top-level classes.

The following table is an exhaustive list of such mappings.

| `androidx.paging`                             | `app.cash.paging`                           |
|-----------------------------------------------|---------------------------------------------|
| `LoadState.NotLoading`                        | `LoadStateNotLoading`                       |
| `LoadState.Loading`                           | `LoadStateLoading`                          |
| `LoadState.Error`                             | `LoadStateError`                            |
| `PagingSource.LoadParams<Key>`                | `PagingSourceLoadParams<Key>`               |
| `PagingSource.LoadParams.Refresh<Key>`        | `PagingSourceLoadParamsRefresh<Key>`        |
| `PagingSource.LoadParams.Append<Key>`         | `PagingSourceLoadParamsAppend<Key>`         |
| `PagingSource.LoadParams.Prepend<Key>`        | `PagingSourceLoadParamsPrepend<Key>`        |
| `PagingSource.LoadResult.Error<Key, Value>`   | `PagingSourceLoadResultError<Key, Value>`   |
| `PagingSource.LoadResult.Invalid<Key, Value>` | `PagingSourceLoadResultInvalid<Key, Value>` |
| `PagingSource.LoadResult.Page<Key, Value>`    | `PagingSourceLoadResultPage<Key, Value>`    |
| `RemoteMediator.MediatorResult`               | `RemoteMediatorMediatorResult`              |
| `RemoteMediator.MediatorResult.Error`         | `RemoteMediatorMediatorResultError`         |
| `RemoteMediator.MediatorResult.Success`       | `RemoteMediatorMediatorResultSuccess`       |
| `RemoteMediator.InitializeAction`             | `RemoteMediatorInitializeAction`            |

### Superclass removed for nested classes in `commonMain` only

#### Issue reference

https://youtrack.jetbrains.com/issue/KT-27412

#### Workaround

Unsafe cast the object to the superclass it _should_ be.
For example:

```kotlin
val loadState = LoadStateLoading as LoadState
```

As `LoadStateLoading` _is_ a subclass of `LoadState` in every target,
such a cast isn't in fact "unsafe".
This cast will cause warnings like the following:

```
> Task compileCommonMainKotlinMetadata
w: /…/src/commonMain/kotlin/Foo.kt: (99, 11): This cast can never succeed

> Task compileKotlinJvm
w: /…/src/commonMain/kotlin/Foo.kt: (99, 11): No cast needed

> Task compileKotlinIosSimulatorArm64
w: /…/src/commonMain/kotlin/Foo.kt: (99, 11): No cast needed
```

You can safely ignore these warnings,
or you can suppress them with `@Suppress("CAST_NEVER_SUCCEEDS", "USELESS_CAST", "KotlinRedundantDiagnosticSuppress")`.

### Nested constants have been top-level constants

#### Issue reference

https://youtrack.jetbrains.com/issue/KT-18856

#### Workaround

Use the newly defined top-level constants.

The following table is an exhaustive list of such mappings.

| `androidx.paging`                                      | `app.cash.paging`  |
|--------------------------------------------------------|--------------------|
| PagingConfig.Companion.MAX_SIZE_UNBOUNDED              | MAX_SIZE_UNBOUNDED |
| PagingSource.LoadResult.Page.Companion.COUNT_UNDEFINED | COUNT_UNDEFINED    |

### Missing `copy` functions for `PagingSourceLoadResultPage` and `PagingSourceLoadResultError`

#### Issue references

https://youtrack.jetbrains.com/issue/KT-18565

https://youtrack.jetbrains.com/issue/KT-54786

#### Workaround

Import the `copy` extension functions via `app.cash.paging.copy`.
