# Change Log

> ❗Looking for the `3.1.1` change log?
> Then check out [this CHANGELOG](https://github.com/cashapp/multiplatform-paging/blob/main/CHANGELOG.md) instead! 

## [Unreleased]

### Added

- [paging-common] Packaged version `3.2.0-alpha05` of AndroidX Paging's `paging-common` for Kotlin/Multiplatform.
- [paging-compose-common] Packaged version `1.0.0-alpha19` of AndroidX Paging's `paging-compose` for Kotlin/Multiplatform.

### Deprecated

- [paging-runtime-composeui] `paging-runtime-composeui` is now functionally equivalent to `paging-compose-common`,
  as the additional functions that `paging-runtime-composeui` provides (i.e., `items` and `itemsIndexed`) have [been deprecated upstream](https://developer.android.com/jetpack/androidx/releases/paging#1.0.0-alpha19).

[Unreleased]: https://github.com/cashapp/multiplatform-paging/compare/3.2.0-alpha05-0.2.1...main-3.2.0-alpha05
