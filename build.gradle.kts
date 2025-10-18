import com.diffplug.gradle.spotless.SpotlessExtension
import com.vanniktech.maven.publish.MavenPublishBaseExtension
import com.vanniktech.maven.publish.SonatypeHost
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
  alias(libs.plugins.mavenPublish) apply false
  alias(libs.plugins.kotlin.serialization) apply false
  alias(libs.plugins.kotlin.multiplatform) apply false
  alias(libs.plugins.kotlin.android) apply false
  alias(libs.plugins.android.application) apply false
  alias(libs.plugins.spotless) apply false
}

allprojects {
  group = "app.cash.paging"
  version = "${rootProject.libs.versions.androidx.paging.get()}-0.6.0-SNAPSHOT"

  plugins.withId("org.jetbrains.kotlin.multiplatform") {
    configure<KotlinMultiplatformExtension> {
      jvmToolchain(JavaLanguageVersion.of(8).asInt())
      compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
      }
    }
  }
  plugins.withId("org.jetbrains.kotlin.jvm") {
    configure<KotlinJvmProjectExtension> {
      jvmToolchain(JavaLanguageVersion.of(8).asInt())
      compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
      }
    }
  }
  plugins.withId("org.jetbrains.kotlin.android") {
    configure<KotlinAndroidProjectExtension> {
      jvmToolchain(JavaLanguageVersion.of(8).asInt())
      compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
      }
    }
  }

  apply(
    plugin =
      rootProject.libs.plugins.spotless
        .get()
        .pluginId,
  )
  configure<SpotlessExtension> {
    kotlin {
      target("**/*.kt")
      targetExclude("upstreams/**/*.kt")
      ktlint(libs.ktlint.get().version)
        .customRuleSets(
          listOf(
            libs.ktlintComposeRules.get().toString(),
          ),
        ).editorConfigOverride(
          mapOf(
            // Disabled because paging-* filenames should be identical to that of AndroidX Paging.
            "ktlint_standard_filename" to "disabled",
            // Do not impose standard Kotlin function naming onto Compose functions.
            "ktlint_function_naming_ignore_when_annotated_with" to "Composable",
          ),
        )
    }
    kotlinGradle {
      target("**/*.kts")
      targetExclude("upstreams/**/*.kts")
      ktlint(libs.ktlint.get().version)
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
