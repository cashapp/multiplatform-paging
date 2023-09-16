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
        implementation(libs.stately.concurrent.collections)
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

configure<MavenPublishBaseExtension> {
  configure(KotlinMultiplatform(javadocJar = JavadocJar.Empty()))
}
