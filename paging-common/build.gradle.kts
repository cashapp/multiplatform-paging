import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinMultiplatform
import com.vanniktech.maven.publish.MavenPublishBaseExtension

plugins {
  id(libs.plugins.kotlin.multiplatform.get().pluginId)
  alias(libs.plugins.mavenPublish)
}

kotlin {
  ios()
  iosSimulatorArm64()
  macosArm64()
  macosX64()
  js(IR) {
    browser()
  }
  jvm()
  linuxX64()
  linuxArm64()
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
        implementation(libs.stately.concurrent.collections)
      }
    }
    val nativeMain by creating {
      kotlin.srcDir("../upstreams/androidx-main/paging/paging-common/src/nonJsMain")
      dependsOn(nonJsMain)
      dependsOn(nonJvmMain)
    }
    val iosMain by getting {
      dependsOn(nativeMain)
    }
    val iosSimulatorArm64Main by getting {
      dependsOn(iosMain)
    }
    val macosArm64Main by getting {
      dependsOn(nativeMain)
    }
    val macosX64Main by getting {
      dependsOn(nativeMain)
    }
    val linuxX64Main by getting {
      dependsOn(nativeMain)
    }
    val linuxArm64Main by getting {
      dependsOn(nativeMain)
    }
    val mingwX64Main by getting {
      dependsOn(nativeMain)
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
