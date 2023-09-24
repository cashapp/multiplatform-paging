import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinMultiplatform
import com.vanniktech.maven.publish.MavenPublishBaseExtension
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType

@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.mavenPublish)
}

@OptIn(ExperimentalKotlinGradlePluginApi::class)
kotlin {
  targetHierarchy.custom {
    common {
      group("nonJs") {
        withCompilations { it.target.platformType != KotlinPlatformType.js }
      }
      group("commonAndroidX") {
        withIos()
        withJvm()
        withLinuxX64()
        withMacos()
      }
      group("commonNonAndroidX") {
        group("jsAndNonAndroidX") {
          withJs()
        }
        group("nonJsAndNonAndroidX") {
          group("nativeAndNonAndroidX") {
            withMingw()
            withLinuxArm64()
            withTvos()
            withWatchos()
          }
        }
      }
    }
  }

  js(IR) {
    nodejs()
    binaries.executable()
  }

  jvm()

  ios()
  iosSimulatorArm64()
  linuxArm64()
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
        implementation(projects.pagingCommon)
        implementation(libs.kotlin.stdlib.common)
        implementation(libs.kotlinx.coroutines.core)
      }
    }
    val nonJsMain by getting
    val commonAndroidXMain by getting {
      dependencies {
        api(libs.androidx.paging.testing)
      }
    }
    val commonNonAndroidXMain by getting {
      kotlin.srcDir("../upstreams/androidx-main/paging/paging-testing/src/commonMain")
    }
    val nonJsAndNonAndroidXMain by getting {
      dependsOn(nonJsMain)
      kotlin.srcDir("../upstreams/androidx-main/paging/paging-testing/src/nonJsMain")
    }
    val jsAndNonAndroidXMain by getting {
      kotlin.srcDir("../upstreams/androidx-main/paging/paging-testing/src/jsMain")
    }
    val nativeAndNonAndroidXMain by getting {
      kotlin.srcDir("../upstreams/androidx-main/paging/paging-testing/src/nativeMain")
      dependencies {
        implementation(libs.kotlinx.atomicfu)
      }
    }
  }
}

configure<MavenPublishBaseExtension> {
  configure(KotlinMultiplatform(javadocJar = JavadocJar.Empty()))
}
