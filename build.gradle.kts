import io.gitlab.arturbosch.detekt.Detekt

// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:${Versions.gradle}")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.kotlin}")
        classpath("com.google.gms:google-services:${Versions.google_services}")
        classpath("com.google.android.libraries.mapsplatform.secrets-gradle-plugin:secrets-gradle-plugin:${Versions.secrets_gradle_plugin}")

        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:${Versions.navigation_safe_args_gradle_plugin}")

        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle files
    }
}

plugins {
    id("io.gitlab.arturbosch.detekt").version(Versions.detekt)
}

detekt {
    toolVersion = Versions.detekt
    buildUponDefaultConfig = true // preconfigure defaults
    allRules = true // activate all available (even unstable) rules.
    config = files("$projectDir/config/detekt.yml") // point to your custom config defining rules to run, overwriting default behavior
    baseline = file("$projectDir/config/baseline.xml") // a way of suppressing issues before introducing detekt

    source = files("$projectDir/app/src/main/kotlin")

    reports {
        xml {
            xml.enabled = true
            xml.destination = file("$projectDir/build/detekt/report.xml")
        }
        html {
            html.enabled = true
            html.destination = file("$projectDir/build/detekt/report.html")
        }
        txt {
            txt.enabled = true
            txt.destination = file("$projectDir/build/detekt/report.txt")
        }
    }
}

// Kotlin DSL
tasks.withType<Detekt>().configureEach {
    // Target version of the generated JVM bytecode. It is used for type resolution.
    jvmTarget = "1.8"
}

dependencies {
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:${Versions.detekt}")
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
    configurations.all {
        resolutionStrategy {
            //force 'asm:asm-all:3.3.1', 'commons-io:commons-io:1.4'
            force("org.objenesis:objenesis:${Versions.objenesis}")
            force("androidx.test:monitor:${Versions.testMonitor}")
        }
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}