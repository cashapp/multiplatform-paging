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
  targetHierarchy.default()

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
      }
    }
    val nonJsMain by creating {
      dependsOn(commonMain)
    }
    val jvmMain by getting {
      dependencies {
        api(libs.androidx.paging.common)
      }
    }
    val nonJvmMain by creating {
      kotlin.srcDir("../upstreams/androidx-main/paging/paging-common/src/commonMain")
      dependsOn(commonMain)
      dependencies {
        implementation(libs.stately.concurrency)
        implementation(libs.stately.iso.collections)
      }
    }
    val nonJsAndNonJvmMain by creating {
      kotlin.srcDir("../upstreams/androidx-main/paging/paging-common/src/nonJsMain")
      dependsOn(nonJvmMain)
    }
    val jsMain by getting {
      kotlin.srcDir("../upstreams/androidx-main/paging/paging-common/src/jsMain")
    }
    targets.forEach { target ->
      if (target.platformType == KotlinPlatformType.common) return@forEach
      if (target.platformType != KotlinPlatformType.js) {
        target.compilations.getByName("main").defaultSourceSet.dependsOn(nonJsMain)
      }
      if (target.platformType != KotlinPlatformType.jvm) {
        target.compilations.getByName("main").defaultSourceSet.dependsOn(nonJvmMain)
      }
      if (target.platformType !in arrayOf(KotlinPlatformType.js, KotlinPlatformType.jvm)) {
        target.compilations.getByName("main").defaultSourceSet.dependsOn(nonJsAndNonJvmMain)
      }
    }
  }
}

configure<MavenPublishBaseExtension> {
  configure(KotlinMultiplatform(javadocJar = JavadocJar.Empty()))
}
