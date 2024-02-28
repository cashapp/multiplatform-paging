# Change Log

## [Unreleased]


## [3.3.0-alpha02-0.5.1]

### Added

- [paging-compose-common] Added `linuxArm64` target.


## [3.3.0-alpha02-0.5.0]

### Changed

- Compile with Kotlin 1.9.22.

### Breaking

- [paging-compose-common] Compile with JetBrains Compose 1.6.0 which changes the ABI for JS-based Compose libraries. Downstream usage in JS targets will also need to be recompiled.


## [3.3.0-alpha02-0.4.0]

### Added

- [paging-common] Packaged version `3.3.0-alpha02` of AndroidX Paging's `paging-common` for Kotlin/Multiplatform.
- [paging-compose-common] Packaged version `3.3.0-alpha02` of AndroidX Paging's `paging-compose` for Kotlin/Multiplatform.
- [paging-testing] Packaged version `3.3.0-alpha02` of AndroidX Paging's `paging-testing` for Kotlin/Multiplatform.

### Breaking

- [paging-common] With Kotlin 1.9.20, an `expect` with default arguments are no longer permitted when an `actual` is a `typealias` (see [KT-57614](https://youtrack.jetbrains.com/issue/KT-57614)).
  As `paging-common` used this mechanism extensively, most default arguments are no longer available.
  To work around this, you can follow [the migration guide](https://github.com/cashapp/multiplatform-paging/tree/main-3.3.0-alpha02/paging-common#missing-default-values).
- [paging-runtime-composeui] This artifact has been deleted, as all functions have been removed upstream. 
- [paging-runtime-uikit] Instantiating `PagingCollectionViewController` no longer requires an `indexCreator`.
  It can be safely removed.


[Unreleased]: https://github.com/cashapp/multiplatform-paging/compare/3.3.0-alpha02-0.5.0...main-3.3.1-alpha02
[3.3.0-alpha02-0.5.1]: https://github.com/cashapp/multiplatform-paging/releases/tag/3.3.0-alpha02-0.5.1
[3.3.0-alpha02-0.5.0]: https://github.com/cashapp/multiplatform-paging/releases/tag/3.3.0-alpha02-0.5.0
[3.3.0-alpha02-0.4.0]: https://github.com/cashapp/multiplatform-paging/releases/tag/3.3.0-alpha02-0.4.0
