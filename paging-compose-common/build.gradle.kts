import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinMultiplatform
import com.vanniktech.maven.publish.MavenPublishBaseExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType

@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
  id("com.android.library")
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.jetbrains.compose)
  alias(libs.plugins.mavenPublish)
}

kotlin {
  android {
    publishLibraryVariants("release")
  }

  js(IR) {
    nodejs()
    binaries.executable()
  }

  jvm()

  ios()
  iosSimulatorArm64()
  linuxX64()
  macosArm64()
  macosX64()
  mingwX64()
  tvos()
  tvosSimulatorArm64()
  watchos()
  watchosSimulatorArm64()

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
    val jsMain by getting {}
    targets.forEach { target ->
      if (target.platformType == KotlinPlatformType.common) return@forEach
      if (target.platformType != KotlinPlatformType.androidJvm) {
        // Not using a `nonAndroidMain` source set because the androidx-main branch srcDir has dependencies on
        // `androidx.paging`. `app.cash.paging` has to be used within `commonMain`, and would mean the androidx-main
        // branch depend would have to depend on main, resulting in a cyclical dependency. Multiplatformized variants
        // of `androidx.paging` IS available when we're not in `commonMain`, so explicitly depending on the nonAndroidMain
        // srcDir means that `androidx.paging` is in the classpath and will compile successfully.
        target.compilations.getByName("main").defaultSourceSet.kotlin.srcDirs(
          "src/nonAndroidMain",
          "../upstreams/androidx-main/paging/paging-compose/src/commonMain",
        )
      }
    }
  }
}

android {
  namespace = "app.cash.paging.compose"
  compileSdk = 34

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
