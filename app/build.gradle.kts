import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
}

// Reads `apiBaseUrl=...` from local.properties (gitignored, per-machine) so each dev can
// point at their own backend without touching a committed file. Falls back to the
// placeholder below when the key is absent, so a fresh checkout still builds.
val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) {
        file.inputStream().use { load(it) }
    }
}
val apiBaseUrl: String = localProperties.getProperty("apiBaseUrl") ?: "https://api.innogeeks.example"

android {
    namespace = "edu.kiet.innogeeks"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "edu.kiet.innogeeks"
        minSdk = 24
        //noinspection OldTargetApi
        targetSdk = 36
        versionCode = 6
        versionName = "4.1.0"

        buildConfigField("String", "BASE_URL", "\"$apiBaseUrl\"")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        // FULL keeps native symbol names in the App Bundle so Play Console can deobfuscate NDK crashes.
        ndk {
            debugSymbolLevel = "FULL"
        }
    }

    buildTypes {
        release {
            // Play Store requires a minified/shrunk release build; proguard-rules.pro has no
            // custom keep rules yet, so watch for reflection-based crashes in a release build.
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }

}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.text.google.fonts)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // Haze — real backdrop blur for glassmorphism (glass nav + login card)
    implementation(libs.haze)
    implementation(libs.haze.blur)
    implementation(libs.haze.blur.materials)

    // Lottie — circles.json animated background on the domain collapsed cards
    implementation(libs.lottie.compose)

    // Koin — dependency injection
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)

    // Ktor — networking (OkHttp engine)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.client.logging)
    implementation(libs.ktor.client.auth)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)

    // kotlinx-datetime — java.time needs desugaring below minSdk 24; this doesn't
    implementation(libs.kotlinx.datetime)

    // Room — local database (offline-first source of truth)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // DataStore — persisted credentials/tokens
    implementation(libs.androidx.datastore.preferences)

    // Navigation + ViewModel in Compose
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
