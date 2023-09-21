import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinMultiplatform
import com.vanniktech.maven.publish.MavenPublishBaseExtension
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.mavenPublish)
}

@OptIn(ExperimentalKotlinGradlePluginApi::class)
kotlin {
  targetHierarchy.default()

  ios()
  iosSimulatorArm64()
  macosArm64()
  macosX64()
  js(IR) {
    browser()
  }
  jvm()
  linuxX64()
  mingwX64()

  sourceSets {
    all {
      languageSettings {
        optIn("androidx.paging.ExperimentalPagingApi")
        optIn("kotlin.RequiresOptIn")
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
    val nativeMain by getting {
      kotlin.srcDir("../upstreams/androidx-main/paging/paging-common/src/nonJsMain")
      dependsOn(nonJsMain)
      dependsOn(nonJvmMain)
    }
    val jsMain by getting {
      kotlin.srcDir("../upstreams/androidx-main/paging/paging-common/src/jsMain")
      dependsOn(nonJvmMain)
    }
  }
}

configure<MavenPublishBaseExtension> {
  configure(KotlinMultiplatform(javadocJar = JavadocJar.Empty()))
}
