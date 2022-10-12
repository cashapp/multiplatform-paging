plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.kotlin.serialization)
}

kotlin {
  jvm()

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
    val jvmMain by getting {
      dependencies {
        implementation(libs.ktor.client.okhttp)
      }
    }
  }
}
