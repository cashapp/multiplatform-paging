# Contributing

## Building This Project

This project uses Git submodules to build our branch of AndroidX Paging alongside our common API declarations.

You'll need to run these commands before you can build:

```bash
git submodule init
```

## Directory Structure

- [paging-common/src/commonMain](paging-common/src/commonMain)
  - Defines the common multiplatform API.
    This is the paging3 subset of `androidx.paging`.
  - Exclusively `expect` types with package name `app.cash.paging`.

- [paging-common/src/jvmMain](paging-common/src/jvmMain)
  - Only `typealias` declarations from `app.cash.paging` to `androidx.paging`.
  - This has a library dependency on androidx.paging.
    We depend on Google's official releases on Android.

- [paging-common/src/nonJvmMain](paging-common/src/nonJvmMain)
  - Only `typealias` declarations from `app.cash.paging` to `androidx.paging`.
  - Includes sources from our branch of `androidx.paging` in the `upstreams/androidx-main` directory.

- [upstreams/androidx-main](upstreams/androidx-main)
  - the androidx repo, branched to work on non-JVM platforms
  - our branch does this:
    - prunes everything that isn't paging-related
    - changes paging internals to support non-JVM platforms
