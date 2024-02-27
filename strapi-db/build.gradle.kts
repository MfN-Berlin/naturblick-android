group = "berlin.mfn.naturblick"
version = "1.0.0"

plugins {
  kotlin("jvm") version "1.6.21"
  kotlin("plugin.serialization") version "1.6.21"
  id("java-gradle-plugin")
}

repositories {
  google()
  mavenCentral()
}

dependencies {
  implementation(kotlin("stdlib", "1.6.21"))
  implementation("com.squareup.retrofit2:retrofit:2.9.0")
  implementation("com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:0.8.0")
  implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.1")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.1")
  implementation("org.xerial:sqlite-jdbc:3.36.0.3")
  implementation("com.android.tools:sdk-common:30.2.1")
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
