// Copyright (c) Cosmo Tech.
// Licensed under the MIT license.
import com.diffplug.gradle.spotless.SpotlessExtension
import io.gitlab.arturbosch.detekt.Detekt

plugins {
  val kotlinVersion = "1.7.20"
  kotlin("jvm") version kotlinVersion
  id("com.diffplug.spotless") version "6.11.0"
  id("io.gitlab.arturbosch.detekt") version "1.21.0"
  id("pl.allegro.tech.build.axion-release") version "1.14.2"
  `maven-publish`
  // Apply the java-library plugin for API and implementation separation.
  `java-library`
}

scmVersion { tag { prefix.set("") } }

val kotlinJvmTarget = 17

java { toolchain { languageVersion.set(JavaLanguageVersion.of(kotlinJvmTarget)) } }

publishing {
  repositories {
    maven {
      name = "GitHubPackages"
      url = uri("https://maven.pkg.github.com/Cosmo-Tech/cosmotech-api-azure-cosmos-db")
      credentials {
        username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_ACTOR")
        password = project.findProperty("gpr.key") as String? ?: System.getenv("GITHUB_TOKEN")
      }
    }
  }

  publications {
    create<MavenPublication>("maven") {
      groupId = "com.github.Cosmo-Tech"
      artifactId = "cosmotech-api-azure-cosmos-db"
      version = scmVersion.version
      pom {
        name.set("Cosmo Tech API Azure CosmosDB")
        description.set("Cosmo Tech API Azure library for CosmosDB")
        url.set("https://github.com/Cosmo-Tech/cosmotech-api-azure-cosmos-db")
        licenses {
          license {
            name.set("MIT License")
            url.set("https://github.com/Cosmo-Tech/cosmotech-api-azure-cosmos-db/blob/main/LICENSE")
          }
        }
      }

      from(components["java"])
    }
  }
}

repositories {
  maven {
    name = "GitHubPackages"
    url = uri("https://maven.pkg.github.com/Cosmo-Tech/cosmotech-api-common")
    credentials {
      username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_ACTOR")
      password = project.findProperty("gpr.key") as String? ?: System.getenv("GITHUB_TOKEN")
    }
  }

  mavenCentral()
}

configure<SpotlessExtension> {
  isEnforceCheck = false

  val licenseHeaderComment =
      """
        // Copyright (c) Cosmo Tech.
        // Licensed under the MIT license.
      """
          .trimIndent()

  java {
    googleJavaFormat()
    target("**/*.java")
    licenseHeader(licenseHeaderComment)
  }
  kotlin {
    ktfmt("0.41")
    target("**/*.kt")
    licenseHeader(licenseHeaderComment)
  }
  kotlinGradle {
    ktfmt("0.41")
    target("**/*.kts")
    //      licenseHeader(licenseHeaderComment, "import")
  }
}

tasks.withType<Detekt>().configureEach {
  buildUponDefaultConfig = true // preconfigure defaults
  allRules = false // activate all available (even unstable) rules.
  config.from(file("$rootDir/.detekt/detekt.yaml"))
  jvmTarget = kotlinJvmTarget.toString()
  ignoreFailures = project.findProperty("detekt.ignoreFailures")?.toString()?.toBoolean() ?: false
  // Specify the base path for file paths in the formatted reports.
  // If not set, all file paths reported will be absolute file path.
  // This is so we can easily map results onto their source files in tools like GitHub Code
  // Scanning
  basePath = rootDir.absolutePath
  reports {
    html {
      // observe findings in your browser with structure and code snippets
      required.set(true)
      outputLocation.set(file("$buildDir/reports/detekt/${project.name}-detekt.html"))
    }
    xml {
      // checkstyle like format mainly for integrations like Jenkins
      required.set(false)
      outputLocation.set(file("$buildDir/reports/detekt/${project.name}-detekt.xml"))
    }
    txt {
      // similar to the console output, contains issue signature to manually edit baseline files
      required.set(true)
      outputLocation.set(file("$buildDir/reports/detekt/${project.name}-detekt.txt"))
    }
    sarif {
      // standardized SARIF format (https://sarifweb.azurewebsites.net/) to support integrations
      // with Github Code Scanning
      required.set(true)
      outputLocation.set(file("$buildDir/reports/detekt/${project.name}-detekt.sarif"))
    }
  }
}

tasks.jar {
  manifest {
    attributes(
        mapOf("Implementation-Title" to project.name, "Implementation-Version" to project.version))
  }
}
// Dependencies version
val deteckVersion = "1.21.0"
val azureSDKBomVersion = "1.2.7"
val azureSpringBootBomVersion = "3.14.0"
val cosmotechApiCommonVersion = "0.1.32-SNAPSHOT"
val springBootStarterWebVersion = "3.0.3"

dependencies {
  detekt("io.gitlab.arturbosch.detekt:detekt-cli:$deteckVersion")
  detekt("io.gitlab.arturbosch.detekt:detekt-formatting:$deteckVersion")

  api("com.github.Cosmo-Tech:cosmotech-api-common:$cosmotechApiCommonVersion")

  // Azure
  implementation(platform("com.azure.spring:azure-spring-boot-bom:$azureSpringBootBomVersion"))
  api(platform("com.azure:azure-sdk-bom:$azureSDKBomVersion"))
  api("com.azure.spring:azure-spring-boot-starter-cosmos")

  // Spring
  implementation(
      "org.springframework.boot:spring-boot-starter-web:${springBootStarterWebVersion}") {
        exclude(group = "org.springframework.boot", module = "spring-boot-starter-tomcat")
      }
  implementation(
      "org.springframework.boot:spring-boot-starter-actuator:$springBootStarterWebVersion")

  // Align versions of all Kotlin components
  implementation(platform("org.jetbrains.kotlin:kotlin-bom"))

  // Use the Kotlin JDK 8 standard library.
  implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

  // Use the Kotlin test library.
  testImplementation("org.jetbrains.kotlin:kotlin-test")

  // Use the Kotlin JUnit integration.
  testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
}
