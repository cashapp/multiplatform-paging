plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.mavenPublish)
}

kotlin {
  ios()
  js(IR) {
    browser()
  }
  jvm()

  sourceSets {
    all {
      languageSettings {
        optIn("androidx.paging.ExperimentalPagingApi")
      }
    }
    val commonMain by getting {
      dependencies {
        implementation(libs.kotlin.stdlib.common)
        implementation(libs.kotlinx.coroutines.core)
      }
    }
    val nonJsMain by creating {
      dependsOn(commonMain)
    }
    val jvmMain by getting {
      dependsOn(nonJsMain)
      dependencies {
        implementation(libs.androidx.paging.common)
      }
    }
    val nonJvmMain by creating {
      kotlin.srcDir("../upstreams/androidx-main-mpp/paging/paging-common/src/commonMain")
      dependsOn(commonMain)
      dependencies {
        implementation(libs.stately.concurrency)
        implementation(libs.stately.iso.collections)
      }
    }
    val iosMain by getting {
      kotlin.srcDir("../upstreams/androidx-main-mpp/paging/paging-common/src/nonJsMain", )
      dependsOn(nonJsMain)
      dependsOn(nonJvmMain)
    }
    val jsMain by getting {
      kotlin.srcDir("../upstreams/androidx-main-mpp/paging/paging-common/src/jsMain", )
      dependsOn(nonJvmMain)
    }
  }
}
