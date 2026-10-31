plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.deerclops99.notesbyvicsign99"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.deerclops99.notesbyvicsign99"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
    debug {
        manifestPlaceholders["admobAppId"] = "ca-app-pub-3940256099942544~3347511713"
        buildConfigField("String", "ADMOB_BANNER_ID", "\"ca-app-pub-3940256099942544/9214589741\"")
    }
    release {
        manifestPlaceholders["admobAppId"] = "ca-app-pub-7680074184799717~5179790931"
        buildConfigField("String", "ADMOB_BANNER_ID", "\"ca-app-pub-7680074184799717/4190590003\"")
        optimization {
            enable = true
            packageScope = setOf("androidx.**", "kotlin.**", "kotlinx.**")
        }
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
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
    implementation("androidx.compose.material:material-icons-core")
    // AppCompat: idioma dentro de la app y modo oscuro elegible
    implementation("androidx.appcompat:appcompat:1.7.0")
    // Room: base de datos local
    implementation("androidx.room:room-runtime:2.8.4")
    ksp("androidx.room:room-compiler:2.8.4")
    // ViewModel integrado con Compose
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.0")
    // Glance: widgets con sintaxis tipo Compose
    implementation("androidx.glance:glance-appwidget:1.1.1")
    implementation("androidx.glance:glance-material3:1.1.1")
    // AdMob
    implementation("com.google.android.gms:play-services-ads:24.4.0")
}