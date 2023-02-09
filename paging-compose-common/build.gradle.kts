import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinMultiplatform
import com.vanniktech.maven.publish.MavenPublishBaseExtension

@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
  id("com.android.library")
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.jetbrains.compose)
  alias(libs.plugins.mavenPublish)
}

kotlin {
  android()
  ios()
  iosSimulatorArm64()
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
        implementation(compose.runtime)
        api(projects.pagingCommon)
      }
    }
    val androidMain by getting {
      dependencies {
        api(libs.androidx.paging.compose)
      }
    }
    val jvmMain by getting {
      // Not using a `nonAndroidMain` source set because the androidx-main branch srcDir has dependencies on
      // `androidx.paging`. `app.cash.paging` has to be used within `commonMain`, and would mean the androidx-main
      // branch depend would have to depend on main, resulting in a cyclical dependency. Multiplatformized variants
      // of `androidx.paging` IS available when we're not in `commonMain`, so explicitly depending on the nonAndroidMain
      // srcDir means that `androidx.paging` is in the classpath and will compile successfully.
      kotlin.srcDirs(
        "src/nonAndroidMain",
        "../upstreams/androidx-main/paging/paging-compose/src/commonMain",
      )
    }
    val iosMain by getting {
      kotlin.srcDirs(
        "src/nonAndroidMain",
        "../upstreams/androidx-main/paging/paging-compose/src/commonMain",
      )
    }
    val iosSimulatorArm64Main by getting {
      dependsOn(iosMain)
    }
    val jsMain by getting {
      kotlin.srcDirs(
        "src/nonAndroidMain",
        "../upstreams/androidx-main/paging/paging-compose/src/commonMain",
      )
    }
  }
}

android {
  namespace = "app.cash.paging.compose"
  compileSdk = 33

  defaultConfig {
    minSdk = 21
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
}

configure<MavenPublishBaseExtension> {
  configure(KotlinMultiplatform(javadocJar = JavadocJar.Empty()))
}
