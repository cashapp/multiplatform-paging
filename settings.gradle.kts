rootProject.name = "multiplatform-paging-root"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
  repositories {
    gradlePluginPortal()
    mavenCentral()
    google()
  }
}

include(":paging-common") // This is done
include(":paging-compose-common") // Copy-paste from
include(":paging-runtime-uikit")
include(":paging-testing")
include(":samples:repo-search:shared")
include(":samples:repo-search:shared-composeui")
include(":samples:repo-search:android-composeui")
include(":samples:repo-search:desktop-composeui")
