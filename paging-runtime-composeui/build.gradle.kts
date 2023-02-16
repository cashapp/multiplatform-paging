import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinMultiplatform
import com.vanniktech.maven.publish.MavenPublishBaseExtension

@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.jetbrains.compose)
  id("com.android.library")
  alias(libs.plugins.kotlin.parcelize)
  alias(libs.plugins.mavenPublish)
}

android {
  namespace = "app.cash.paging"
  compileSdk = 33
  defaultConfig {
    minSdk = 21
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
}

kotlin {
  jvm()
  android()

  sourceSets {
    all {
      languageSettings {
        optIn("androidx.paging.ExperimentalPagingApi")
      }
    }
    val commonMain by getting {
      dependencies {
        implementation(libs.kotlinx.coroutines.core)
        api(projects.pagingCommon)
        implementation(compose.runtime)
        implementation(compose.foundation)
      }
    }

    val jvmMain by getting {
      dependencies {
        implementation(libs.kotlinx.coroutines.swing)
      }
    }

    val androidMain by getting {
      dependencies {
        implementation(libs.kotlinx.coroutines.android)
      }
    }
  }
}

configure<MavenPublishBaseExtension> {
  configure(KotlinMultiplatform(javadocJar = JavadocJar.Empty()))
}
