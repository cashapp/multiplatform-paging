@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.jetbrains.compose)
  id("com.android.library")
  alias(libs.plugins.kotlin.native.cocoapods)
}

android {
  namespace = "app.cash.paging.samples.reposearch.shared.composeui"
  compileSdk = 33
  defaultConfig {
    minSdk = 21
    targetSdk = 33
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
}

kotlin {
  jvm()
  android()

  cocoapods {
    summary = "Shared Compose UI code for Repo Search."
    homepage = "https://github.com/cashapp/multiplatform-paging/tree/main/samples/repo-search/shared-composeui"
  }

  sourceSets {
    val commonMain by getting {
      dependencies {
        api(projects.samples.repoSearch.shared)
        implementation(projects.pagingRuntimeComposeui)
        implementation(compose.ui)
        implementation(compose.material)
      }
    }
  }
}
