# Contributing

Before code can be accepted, all contributors must complete our [Individual Contributor License Agreement (CLA)](http://squ.re/sign-the-cla).

## Build

This project uses Git submodules to build our branch of AndroidX Paging alongside our common API declarations.

You'll need to run these commands before you can build:

```bash
git submodule update --init --remote
```

There are two primary branches:

* [`androidx-main`](https://github.com/cashapp/multiplatform-paging/tree/androidx-main) – a pruned fork of https://github.com/androidx/androidx containing the multiplatformized `paging-common` and `paging-runtime` code.
* [`main`](https://github.com/cashapp/multiplatform-paging/tree/main) – contains type aliases to the multiplatformized code in `androidx-main` on iOS, and type aliases to the `androidx.paging:paging-X` artifact on JVM.
  The submodule [`upstreams/androidx-main`](upstreams/androidx-main) points to the `androidx-main` branch.

## Add support for another AndroidX Paging release

1. Locate the desired release on the [AndroidX Paging releases page](https://developer.android.com/jetpack/androidx/releases/paging).
2. Click the 'Version X contains these commits' link.
3. Copy the second SHA in the URL.
   For example, the second SHA in the [3.1.1 release commits](https://android.googlesource.com/platform/frameworks/support/+log/04b73e954d139340d0ac8b00cdcef55b103ba393..65c8f2c53158200a61e0e1cc012cdbbadaee60ab/paging) would be 65c8f2c53158200a61e0e1cc012cdbbadaee60ab.
4. `git fetch https://android.googlesource.com/platform/frameworks/support COPIED_SHA`
5. Cherry-pick the Multiplatform Paging commits from androidx-main and resolve conflicts.
