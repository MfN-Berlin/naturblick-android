/*
 * Copyright © 2024 Museum für Naturkunde Berlin.
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

group = "berlin.mfn.naturblick"
version = "1.0.0"

plugins {
  kotlin("jvm") version "1.9.25"
  kotlin("plugin.serialization") version "1.9.25"
  id("java-gradle-plugin")
}

repositories {
  google()
  mavenCentral()
}

dependencies {
  implementation(kotlin("stdlib", "1.9.25"))
  implementation("com.squareup.retrofit2:retrofit:2.9.0")
  implementation("com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:0.8.0")
  implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.1")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
  implementation("com.android.tools:sdk-common:31.5.0")
  testImplementation("io.kotlintest:kotlintest-runner-junit5:3.3.1")
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
