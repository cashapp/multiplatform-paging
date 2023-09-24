# Change Log

## [Unreleased]

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

[Unreleased]: https://github.com/cashapp/multiplatform-paging/compare/3.3.0-alpha02-0.4.0...main-3.3.0-alpha02
