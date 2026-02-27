plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android") version "1.9.22"
}

android {
    namespace = "xyz.hvdw.altitude"
    compileSdk = 34

    defaultConfig {
        applicationId = "xyz.hvdw.altitude"
        minSdk = 23
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

        // Only include the ABIs you want in the final APK
        ndk {
            abiFilters += listOf("arm64-v8a", "x86_64")
        }
    }

    // Enable shrinking + minification
    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }

        getByName("debug") {
            // Keep debug builds fast
            isMinifyEnabled = false
            isShrinkResources = false
        }
    }

    // Remove unwanted native libs if any dependency tries to include them
    packaging {
        jniLibs {
            excludes += listOf(
                "**/armeabi/**",
                "**/armeabi-v7a/**",
                "**/x86/**"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.12.0")
    implementation("com.google.android.gms:play-services-location:21.2.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("org.json:json:20240303")
}
