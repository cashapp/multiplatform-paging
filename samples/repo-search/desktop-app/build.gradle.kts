@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.jetbrains.compose)
}

kotlin {
  jvm {
    withJava()
  }
  sourceSets {
    val jvmMain by getting {
      dependencies {
        implementation(compose.desktop.currentOs)
        implementation(projects.samples.repoSearch.shared)
        api(projects.pagingRuntimeComposeui)
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.6.4")
      }
    }
  }
}

compose.desktop {
  application {
    mainClass = "MainKt"
  }
}
