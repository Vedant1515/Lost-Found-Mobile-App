import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
}

// ── Read MAPS_API_KEY from local.properties ──────────────────────────────────
// ★ Add this line to your local.properties file (project root):
//     MAPS_API_KEY=AIzaSy...yourKeyHere...
val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localPropertiesFile.inputStream().use { localProperties.load(it) }
}
val mapsApiKey = localProperties.getProperty("MAPS_API_KEY") ?: ""

android {
    namespace = "com.example.lostandfound"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.example.lostandfound"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Injects the key into AndroidManifest.xml as ${MAPS_API_KEY}
        manifestPlaceholders["MAPS_API_KEY"] = mapsApiKey

        // Exposes BuildConfig.MAPS_API_KEY for Places SDK initialisation
        buildConfigField("String", "MAPS_API_KEY", "\"$mapsApiKey\"")
    }

    buildFeatures {
        buildConfig = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
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
}

dependencies {
    // ── Existing ─────────────────────────────────────────────────────────────
    implementation(libs.activity)
    implementation(libs.appcompat)
    implementation(libs.constraintlayout)
    implementation(libs.material)
    implementation(libs.recyclerview)

    // ── Geo / Maps (Task 9.1) ─────────────────────────────────────────────
    implementation(libs.play.services.maps)
    implementation(libs.play.services.location)
    implementation(libs.places)

    // ── Testing ───────────────────────────────────────────────────────────
    testImplementation(libs.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.ext.junit)
}
