plugins {
  alias(libs.plugins.kotlin.multiplatform)
}

kotlin {
  js(IR) {
    browser()
  }
  jvm()

  sourceSets {
    val commonMain by getting {
    }
    val jvmMain by getting {
    }
    val nonJvmMain by creating {
      dependsOn(commonMain)
    }
    val jsMain by getting {
      dependsOn(nonJvmMain)
    }
  }
}
