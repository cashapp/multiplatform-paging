@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.jetbrains.compose)
  id("com.android.library")
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.kotlin.native.cocoapods)
}

android {
  namespace = "app.cash.paging.samples.reposearch"
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
  ios()
  iosSimulatorArm64()
  jvm()
  android()

  cocoapods {
    summary = "Shared code for Repo Search."
    homepage = "https://github.com/cashapp/multiplatform-paging/tree/main/samples/repo-search/shared"
  }

  sourceSets {
    val commonMain by getting {
      dependencies {
        api(projects.pagingCommon)
        api(libs.kotlinx.coroutines.core)
        implementation(libs.ktor.client.core)
        implementation(libs.ktor.client.contentNegotiation)
        implementation(libs.ktor.serialization.kotlinx.json)
        implementation(compose.ui)
        implementation(compose.foundation)
        implementation(compose.material)
        implementation(compose.runtime)
      }
    }
    val androidMain by getting {
      dependencies {
        implementation(libs.kotlinx.coroutines.android)
        implementation(libs.ktor.client.okhttp)
      }
    }
    val iosMain by getting {
      dependencies {
        api(projects.pagingRuntimeUikit)
        implementation(libs.ktor.client.darwin)
      }
    }
    val iosSimulatorArm64Main by getting {
      dependsOn(iosMain)
      dependencies {
        api(libs.kotlinx.coroutines.core.iossimulatorarm64)
      }
    }
    val jvmMain by getting {
      dependencies {
        implementation(libs.ktor.client.okhttp)
      }
    }
  }
}
