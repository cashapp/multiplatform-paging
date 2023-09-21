import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinMultiplatform
import com.vanniktech.maven.publish.MavenPublishBaseExtension
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.jetbrains.compose)
  id("com.android.library")
  alias(libs.plugins.mavenPublish)
}

android {
  namespace = "app.cash.paging"
  compileSdk = 34
  defaultConfig {
    minSdk = 21
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
}

@OptIn(ExperimentalKotlinGradlePluginApi::class)
kotlin {
  targetHierarchy.default()

  ios()
  iosSimulatorArm64()
  jvm()
  android {
    publishLibraryVariants("release")
  }

  sourceSets {
    all {
      languageSettings {
        optIn("androidx.paging.ExperimentalPagingApi")
      }
    }
    val commonMain by getting {
      dependencies {
        implementation(libs.kotlinx.coroutines.core)
        api(projects.pagingComposeCommon)
        api(compose.runtime)
        api(compose.foundation)
      }
    }
    val nonAndroidMain by creating {
      dependsOn(commonMain)
    }
    val iosMain by getting {
      dependsOn(nonAndroidMain)
    }
    val jvmMain by getting {
      dependsOn(nonAndroidMain)
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
