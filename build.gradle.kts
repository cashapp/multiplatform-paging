import com.diffplug.gradle.spotless.SpotlessExtension
import com.vanniktech.maven.publish.MavenPublishBaseExtension
import com.vanniktech.maven.publish.SonatypeHost

plugins {
  alias(libs.plugins.android.application) apply false
  alias(libs.plugins.kotlin.android) apply false
  alias(libs.plugins.kotlin.multiplatform) apply false
  alias(libs.plugins.kotlin.native.cocoapods) apply false
  alias(libs.plugins.kotlin.serialization) apply false
  alias(libs.plugins.mavenPublish) apply false
  alias(libs.plugins.spotless) apply false
}

allprojects {
  group = "app.cash.paging"
  version = "${rootProject.libs.versions.androidx.paging.get()}-0.4.0-SNAPSHOT"

  repositories {
    mavenCentral()
    google()
  }

  apply(plugin = rootProject.libs.plugins.spotless.get().pluginId)
  configure<SpotlessExtension> {
    kotlin {
      target("**/*.kt")
      targetExclude("upstreams/**/*.kt")
      ktlint(libs.versions.ktlint.get())
        .editorConfigOverride(
          mapOf(
            // Disabled because paging-* filenames should be identical to that of AndroidX Paging.
            "ktlint_standard_filename" to "disabled",
          ),
        )
    }
    kotlinGradle {
      target("**/*.kts")
      targetExclude("upstreams/**/*.kts")
      ktlint(libs.versions.ktlint.get())
    }
  }

  plugins.withId("com.vanniktech.maven.publish.base") {
    configure<PublishingExtension> {
      repositories {
        maven {
          name = "testMaven"
          url = file("${rootProject.buildDir}/testMaven").toURI()
        }
      }
    }
    @Suppress("UnstableApiUsage")
    configure<MavenPublishBaseExtension> {
      publishToMavenCentral(SonatypeHost.DEFAULT, automaticRelease = true)
      signAllPublications()
      pom {
        description.set("Packages AndroidX's Paging library for Kotlin/Multiplatform.")
        name.set(project.name)
        url.set("https://github.com/cashapp/multiplatform-paging/")
        licenses {
          license {
            name.set("The Apache Software License, Version 2.0")
            url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
            distribution.set("repo")
          }
        }
        developers {
          developer {
            id.set("cashapp")
            name.set("Cash App")
          }
        }
        scm {
          url.set("https://github.com/cashapp/multiplatform-paging/")
          connection.set("scm:git:https://github.com/cashapp/multiplatform-paging.git")
          developerConnection.set("scm:git:ssh://git@github.com/cashapp/multiplatform-paging.git")
        }
      }
    }
  }
}
