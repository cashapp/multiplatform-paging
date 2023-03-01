# Change Log

> ❗Looking for the `3.1.1` change log?
> Then check out [this CHANGELOG](https://github.com/cashapp/multiplatform-paging/blob/main/CHANGELOG.md) instead! 

## [Unreleased]

## [3.2.0-alpha04-0.2.0] - 2022-02-28

### Added

- [paging-common] Packaged version `3.2.0-alpha04` of AndroidX Paging's `paging-common` for Kotlin/Multiplatform.
- [paging-runtime-composeui] A new module, which packages AndroidX Paging's `paging-compose` for Kotlin/Multiplatform, 
  allowing Jetpack Compose UI code to be shared across Android, iOS, and desktop (by [Omid Ghenatnevi](https://github.com/crocsandcoffee)).
- [paging-compose-common] Another new module, which packages a _subset_ of AndroidX Paging's `paging-compose` for Kotlin/Multiplatform.
  Its key difference with `paging-runtime-composeui` is that `paging-compose-common` only depends on Compose Runtime,
  and not Compose UI/Foundation.

[Unreleased]: https://github.com/cashapp/multiplatform-paging/compare/3.2.0-alpha04-0.2.0...main-3.2.0-alpha04
[3.2.0-alpha04-0.2.0]: https://github.com/cashapp/multiplatform-paging/releases/tag/3.2.0-alpha04-0.2.0
