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
        implementation(projects.samples.repoSearch.sharedComposeui)
        implementation(libs.kotlinx.coroutines.swing)
      }
    }
  }
}

compose.desktop {
  application {
    mainClass = "MainKt"
  }
}
