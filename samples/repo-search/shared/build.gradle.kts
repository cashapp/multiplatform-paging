plugins {
  id(libs.plugins.kotlin.multiplatform.get().pluginId)
  id(libs.plugins.kotlin.serialization.get().pluginId)
  id(libs.plugins.kotlin.native.cocoapods.get().pluginId)
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
        api(projects.pagingCommon)
        api(libs.kotlinx.coroutines.core)
        implementation(libs.ktor.client.core)
        implementation(libs.ktor.client.contentNegotiation)
        implementation(libs.ktor.serialization.kotlinx.json)
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
