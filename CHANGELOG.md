# Change Log

> [!IMPORTANT]
> Looking for the `3.2.0-alpha05` change log?
> Then check out [this CHANGELOG](https://github.com/cashapp/multiplatform-paging/blob/main-3.2.0-alpha05/CHANGELOG.md) instead!

## [Unreleased]

## [3.1.1-0.3.1]

### Added

- [paging-common] Add macOS targets (by [Jeff Lockhart](https://github.com/jeffdgr8))

## [3.1.1-0.3.0]

### Added

- [paging-common] Add Linux and MinGW targets (by [Cedric Hippmann](https://github.com/chippmann))

### Deprecated

- [paging-runtime-uikit] Instantiating `PagingCollectionViewController` no longer requires an `indexCreator`.
  It can be safely removed.

## [3.1.1-0.2.0]

### Breaking

- [paging-runtime-uikit] `paging-runtime` renamed to `paging-runtime-uikit` (https://github.com/cashapp/multiplatform-paging/issues/8).

### Fixed

- [paging-common] Type alias `app.cash.paging.ExperimentalPagingApi` to `androidx.paging.ExperimentalPagingApi` (https://github.com/cashapp/multiplatform-paging/issues/6)
- [paging-common] Expose RemoteMediator's public constructor (https://github.com/cashapp/multiplatform-paging/pull/46)

## [3.1.1-0.1.1] - 2022-11-08

### Fixed

- [paging-common] Add missing iOS target
- [paging-runtime] Add missing iOS target

## [3.1.1-0.1.0] - 2022-11-08

Initial release.

[Unreleased]: https://github.com/cashapp/multiplatform-paging/compare/3.1.1-0.3.1...main
[3.1.1-0.3.1]: https://github.com/cashapp/multiplatform-paging/releases/tag/3.1.1-0.3.1
[3.1.1-0.3.0]: https://github.com/cashapp/multiplatform-paging/releases/tag/3.1.1-0.3.0
[3.1.1-0.2.0]: https://github.com/cashapp/multiplatform-paging/releases/tag/3.1.1-0.2.0
[3.1.1-0.1.1]: https://github.com/cashapp/multiplatform-paging/releases/tag/3.1.1-0.1.1
[3.1.1-0.1.0]: https://github.com/cashapp/multiplatform-paging/releases/tag/3.1.1-0.1.0
