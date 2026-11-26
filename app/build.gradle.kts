plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
    id("com.google.gms.google-services") version "4.6.0"
}

android {
    namespace = "com.placefinder.directory"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.placefinder.directory"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        // Support English, Hindi and Nepali from the start
        resConfigs("en", "hi", "ne")
    }

    buildFeatures {
        compose = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{ALPN,LICENSE,NOTICE,ASL2.0}"
        }
    }
}

dependencies {
    // --- App scaffolding ---
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.appcompat:appcompat:1.7.0")

    // --- Google Maps (Android) ---
    implementation("com.google.android.gms:play-services-maps:20.1.0")
    implementation("com.google.android.gms:play-services-location:21.4.0")

    // --- Compose ---
    implementation("androidx.activity:activity-compose:1.10.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.navigation:navigation-compose:2.7.18")
    implementation("androidx.compose.ui:ui:1.7.8")
    implementation("androidx.compose.ui:ui-graphics:1.7.8")
    implementation("androidx.compose.ui:ui-tooling-preview:1.7.8")
    implementation("androidx.compose.runtime:runtime:1.7.8")
    implementation("androidx.compose.foundation:foundation:1.7.8")
    implementation("androidx.compose.material3:material3:1.3.1")

    // --- Database: Room (SQLite via Room) ---
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // --- Stage 4: remote sync via Firestore (requires app/google-services.json) ---
    implementation("com.google.firebase:firebase-firestore:26.6.0")
}
