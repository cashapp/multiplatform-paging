plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.mavenPublish)
}

kotlin {
  ios()
  iosSimulatorArm64()
  jvm()

  sourceSets {
    all {
      languageSettings {
        optIn("androidx.paging.ExperimentalPagingApi")
        optIn("kotlin.RequiresOptIn")
      }
    }
    val commonMain by getting {
      dependencies {
        api(projects.pagingCommon)
        implementation(libs.kotlinx.coroutines.core)
        implementation(libs.stately.concurrency)
        implementation(libs.stately.iso.collections)
      }
    }
    val jvmMain by getting {
      dependencies {
        implementation(libs.androidx.paging.runtime)
      }
    }
    val iosMain by getting {
      kotlin.srcDir("../upstreams/androidx-main/paging/paging-runtime/src/commonMain/kotlin")
    }
    val iosSimulatorArm64Main by getting {
      dependsOn(iosMain)
    }
  }
}
