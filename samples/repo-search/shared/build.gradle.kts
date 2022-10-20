plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.kotlin.native.cocoapods)
}

kotlin {
  ios()
  iosSimulatorArm64()
  jvm()

  cocoapods {
    summary = "Shared code for Repo Search."
    homepage = "https://github.com/cashapp/multiplatform-paging/tree/main/samples/repo-search/shared"
  }

  sourceSets {
    val commonMain by getting {
      dependencies {
        api(projects.pagingRuntime)
        api(libs.kotlinx.coroutines.core)
        implementation(libs.ktor.client.core)
        implementation(libs.ktor.client.contentNegotiation)
        implementation(libs.ktor.serialization.kotlinx.json)
      }
    }
    val iosMain by getting {
      dependencies {
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
