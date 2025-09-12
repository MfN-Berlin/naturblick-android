/*
 * Copyright © 2024 Museum für Naturkunde Berlin.
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

group = "berlin.mfn.naturblick"
version = "1.0.0"

plugins {
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.kotlin.serialization)
  id("java-gradle-plugin")
}

repositories {
  google()
  mavenCentral()
}

dependencies {
  implementation(libs.retrofit)
  implementation(libs.retrofit.serialization)
  implementation(libs.kotlinx.serialization)
  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.sdk.common)
}

tasks.test {
    // Use the built-in JUnit support of Gradle.
    useJUnitPlatform()
}

gradlePlugin {
  plugins {
    create("strapiDbGenerator") {
      id = "berlin.mfn.naturblick.strapidbgenerator"
      implementationClass = "berlin.mfn.naturblick.StrapiDbGenerator"
    }
  }
}
