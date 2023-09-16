plugins {
  id(libs.plugins.android.application.get().pluginId)
  id(libs.plugins.kotlin.android.get().pluginId)
}

android {
  namespace = "app.cash.paging.samples.reposearch"
  compileSdk = 33

  defaultConfig {
    applicationId = "app.cash.paging.samples.reposearch"
    minSdk = 21
    targetSdk = 33
    versionCode = 1
    versionName = "1.0"
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
  }

  kotlinOptions {
    jvmTarget = "1.8"
  }

  buildFeatures {
    compose = true
  }

  composeOptions {
    kotlinCompilerExtensionVersion = libs.versions.androidx.compose.get()
  }
}

dependencies {
  implementation(projects.samples.repoSearch.shared)
  implementation(libs.androidx.activity.compose)
  implementation(libs.androidx.appcompat)
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.paging.compose)
  implementation(libs.androidx.compose.material)
  implementation(libs.androidx.compose.ui)
}
