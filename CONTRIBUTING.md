# Contributing

## Building This Project

This project uses Git submodules to build our branch of AndroidX Paging alongside our common API declarations.

You'll need to run these commands before you can build:

```bash
git submodule update --init --remote
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

## Updating `androidx-main` to release X

1. Locate release X on the [AndroidX Paging releases page](https://developer.android.com/jetpack/androidx/releases/paging).
2. Click the 'Version X contains these commits' link.
3. Copy the second SHA in the URL (`abc123`).
   For example, the second SHA in the [3.1.1 release commits](https://android.googlesource.com/platform/frameworks/support/+log/04b73e954d139340d0ac8b00cdcef55b103ba393..65c8f2c53158200a61e0e1cc012cdbbadaee60ab/paging) would be 65c8f2c53158200a61e0e1cc012cdbbadaee60ab.
4. `git fetch https://android.googlesource.com/platform/frameworks/support abc123`
5. Cherry-pick the Multiplatform Paging commits from androidx-main and resolve conflicts.
