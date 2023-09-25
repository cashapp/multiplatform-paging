# Multiplatform Paging

A library that packages [AndroidX Paging](https://developer.android.com/topic/libraries/architecture/paging/v3-overview) for Kotlin/Multiplatform.

## Introduction

As with AndroidX Paging, the primary modules of Multiplatform Paging are:

* `paging-common` – encompasses the [repository layer](https://developer.android.com/topic/libraries/architecture/paging/v3-overview#repository) and the [view model layer](https://developer.android.com/topic/libraries/architecture/paging/v3-overview#viewmodel)
* `paging-runtime` – encompasses the [UI layer](https://developer.android.com/topic/libraries/architecture/paging/v3-overview#ui)

Unlike AndroidX Paging that makes `paging-common` JVM-specific and `paging-runtime` Android-specific,
Multiplatform Paging makes `paging-common` multiplatform and provides `paging-runtime-uikit`, a UIKit-specific runtime for iOS.
Therefore, pagination logic between Android and iOS can be shared,
and the provided UI components can be used to render the paged items on Android and iOS.

## Usage

For a holistic view of Multiplatform Paging, check out the [GitHub Repository Search sample project](samples/repo-search), where there's an Android and iOS app, along with shared pagination logic.

### `paging-common`

The API of `paging-common` in Multiplatform Paging is identical to that of `paging-common` in AndroidX Paging
(with the exception that: the namespace has changed from `androidx.paging` to `app.cash.paging`;
there are some minor [API discrepancies](paging-common/README.md) due to limitations in the Kotlin compiler).
Therefore, to see how to use `paging-common`, consult the [official documentation of AndroidX Paging](https://developer.android.com/topic/libraries/architecture/paging/v3-overview).

#### JVM

`app.cash.paging:paging-common` on the JVM delegates to `androidx.paging:paging-common` via type aliases.
To understand what this means in practice, see the section [_Interoperability with AndroidX Paging_](#interoperability-with-androidx-paging).

#### iOS

`app.cash.paging:paging-common` on iOS delegates to _our fork_ of AndroidX Paging.

iOS only includes the Paging 3 APIs from AndroidX Paging.
We don't plan on offering Paging 2 support for iOS,
though you can continue to use Paging 2 on the JVM.

### `paging-compose-common`

The API of `paging-compose-common` in Multiplatform Paging is identical to that of `paging-compose` in AndroidX Paging
(with the exception that the namespace has changed from `androidx.paging` to `app.cash.paging`).

To see how to use `paging-compose-common`, consult the [official documentation of AndroidX Paging](https://developer.android.com/reference/kotlin/androidx/paging/compose/package-summary#(kotlinx.coroutines.flow.Flow).collectAsLazyPagingItems(kotlin.coroutines.CoroutineContext)).

#### Android

`app.cash.paging:paging-compose-common` on Android delegates to `androidx.paging:paging-compose` via type aliases.
To understand what this means in practice, see the section [_Interoperability with AndroidX Paging_](#interoperability-with-androidx-paging).

#### Desktop/JVM and iOS

`app.cash.paging:paging-compose-common` on the JVM (but not Android) and iOS delegates to _our fork_ of AndroidX Paging.

### `paging-runtime` for Android

See the [_Interoperability with AndroidX Paging_](#interoperability-with-androidx-paging) section below.

### `paging-runtime-uikit` for iOS

The `PagingCollectionViewController` allows a `PagingData` to be rendered via a `UICollectionView`.
The `PagingCollectionViewController` mimics the `UICollectionViewController`,
providing: the cell count; and item retrieval via `IndexPath`.

Here's an example in Swift:

```swift
final class FooViewController: UICollectionViewController {

  private let delegate = Paging_runtime_uikitPagingCollectionViewController<Foo>()

  private let presenter = …

  required init(coder: NSCoder) {
    super.init(coder: coder)!
    presenter.pagingDatas
      .sink { pagingData in
        self.delegate.submitData(pagingData: pagingData, completionHandler: …)
      }
  }

  override func collectionView(
    _ collectionView: UICollectionView,
    numberOfItemsInSection section: Int
  ) -> Int {
    return Int(delegate.collectionView(collectionView: collectionView, numberOfItemsInSection: Int64(section)))
  }

  override func collectionView(
    _ collectionView: UICollectionView,
    cellForItemAt indexPath: IndexPath
  ) -> UICollectionViewCell {
    let cell = collectionView.dequeueReusableCell(withReuseIdentifier: "FooCell", for: indexPath) as! FooCell

    let item = delegate.getItem(position: Int32(indexPath.row))!
    // …

    return cell
  }
}
```

## Interoperability with AndroidX Paging

As `app.cash.paging:paging-common` on the JVM type aliases to `androidx.paging:paging-common`,
some useful side effects occur:

* The implementation of `app.cash.paging:paging-common` on the JVM is **identical** to `androidx.paging:paging-common`.
  This means that it is impossible for there to be a behavioral discrepancy when using `app.cash.paging:paging-common` on the JVM.
* All libraries that depend on `androidx.paging:paging-common` can continue to be used on the JVM (e.g., `androidx.paging:paging-runtime`, `androidx.paging:paging-compose`, `androidx.paging:paging-rxjava3`).
  This is why there aren't additional `paging-runtime` artifacts to support Android's UI toolkit,
  as you can instead depend on the official AndroidX artifact.
* If you're already using AndroidX Paging, you don't need to refactor your Android code to use Multiplatform Paging.
  The use of Multiplatform Paging is only necessary if you wish to share pagination logic in common code and/or paginate on iOS. 

## Releases

The versioning scheme is of the form `X-Y` where:

- `X` is the AndroidX Paging version that is being tracked.
- `Y` is the Multiplatform Paging version.

For example, if AndroidX Paging is on `3.3.0-alpha02` and Multiplatform Paging is on `0.4.0`,
the artifact for a release of `paging-common` will be `app.cash.paging:paging-common:3.3.0-alpha02-0.4.0`.

### `paging-common` for common

```kotlin
implementation("app.cash.paging:paging-common:3.3.0-alpha02-0.4.0")
```

### `paging-compose-common` for common

```kotlin
implementation("app.cash.paging:paging-compose-common:3.3.0-alpha02-0.4.0")
```

### `paging-runtime-uikit` for iOS

```kotlin
implementation("app.cash.paging:paging-runtime-uikit:3.3.0-alpha02-0.4.0")
```

### Android

Use the [official AndroidX Paging dependencies](https://developer.android.com/jetpack/androidx/releases/paging#declaring_dependencies).

```kotlin
implementation("androidx.paging:paging-runtime:3.3.0-alpha02")
implementation("androidx.paging:paging-compose:3.3.0-alpha02")
implementation("androidx.paging:paging-rxjava3:3.3.0-alpha02")
// etc.
```

## License

    Copyright 2022 Block, Inc.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
