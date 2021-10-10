plugins {
    id("com.android.application")
    id("kotlin-parcelize")
    kotlin("android")
    kotlin("kapt")
    id("com.google.gms.google-services")
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
    id("androidx.navigation.safeargs.kotlin")
}

android {
    compileSdk = DefaultConfiguration.compileSdk

    defaultConfig {
        applicationId = "com.openclassrooms.realestatemanager"
        minSdk = DefaultConfiguration.minSdk
        targetSdk = DefaultConfiguration.targetSdk
        versionCode = DefaultConfiguration.versionCode
        versionName = DefaultConfiguration.versionName

        testInstrumentationRunner = DefaultConfiguration.androidTestInstrumentation
        multiDexEnabled = true
        vectorDrawables.useSupportLibrary = true
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android.txt"),
                "proguard-rules.pro"
            )
        }
    }

    viewBinding {
        android.buildFeatures.viewBinding = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    lint {
        disable("RestrictedApi")
    }

    testOptions {
//        execution 'ANDROIDX_TEST_ORCHESTRATOR'
        unitTests.isIncludeAndroidResources = true
    }

    sourceSets {

        sourceSets.getByName("debug") {
            res {
                srcDirs("src/debug/res")
            }
            assets {
                srcDirs("src/debug/assets")
            }
            resources {
                srcDirs("src/debug/resources")
            }
        }

        sourceSets.getByName("release") {
            kotlin.srcDir("src/release/kotlin")
        }

        sourceSets.getByName("test") {
            kotlin.srcDirs("src/test/kotlin", "src/sharedTest/kotlin")
        }

        sourceSets.getByName("androidTest") {
            kotlin.srcDirs("src/androidTest/kotlin", "src/sharedTest/kotlin")
        }
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().all {
        kotlinOptions {
            jvmTarget = "1.8"
        }
    }

    // Instead, use the bundle block to control which types of configuration APKs
    // you want your app bundle to support.
    bundle {
        language {
            // This property is set to true by default.
            // You can specify `false` to turn off
            // generating configuration APKs for language resources.
            // These resources are instead packaged with each base and
            // feature APK.
            // Continue reading below to learn about situations when an app
            // might change setting to `false`, otherwise consider leaving
            // the default on for more optimized downloads.
            enableSplit = true
        }
        density {
            // This property is set to true by default.
            enableSplit = true
        }
        abi {
            // This property is set to true by default.
            enableSplit = true
        }
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

    kapt(Dependencies.kaptLibraries)
    implementation(platform(Dependencies.firebaseBom))
    implementation(Dependencies.appLibraries)

    kaptAndroidTest(Dependencies.kaptAndroidTestLibraries)
    testImplementation(Dependencies.testLibraries)
    androidTestImplementation(Dependencies.androidTestLibraries)

    androidTestImplementation (Dependencies.espressoContrib) {
        exclude(module = "protobuf-lite")
        exclude(group = "org.checkerframework", module = "checker")
    }

    debugImplementation(Dependencies.debugLibraries)
}