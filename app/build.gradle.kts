plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.google.services)
    alias(libs.plugins.secrets)
}

android {
    namespace = "com.aistudio.perfumatico"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.aistudio.perfumatico.kqpv"
        minSdk = 24
        targetSdk = 36
        versionCode = 31
        versionName = "4.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        val rawGeminiKey = (System.getenv("GEMINI_API_KEY_NEW")?.trim() ?: "").ifEmpty {
            System.getenv("GEMINI_API_KEY")?.trim() ?: ""
        }
        val maskedGeminiKey = if (rawGeminiKey.isNotEmpty()) {
            rawGeminiKey.map { (it.code xor 0x5A).toString() }.joinToString(",")
        } else {
            ""
        }
        buildConfigField("String", "GEMINI_KEY_MASKED", "\"$maskedGeminiKey\"")
    }

    signingConfigs {
        create("debugConfig") {
            storeFile = file("${rootDir}/debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
    }

    buildTypes {
        debug {
            signingConfig = signingConfigs.getByName("debugConfig")
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

val envFile = rootProject.file(".env")
if (!envFile.exists() || envFile.readText().contains("AQ.")) {
    envFile.writeText("GEMINI_API_KEY=PROTECTED\nGEMINI_API_KEY_NEW=PROTECTED\n")
}

secrets {
    propertiesFileName = ".env"
    defaultPropertiesFileName = ".env.example"
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.navigation.compose)
    
    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Image loading
    implementation(libs.coil.compose)

    // JSON parsing for fragrance database
    implementation(libs.gson)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.play.services.auth)

    // Gemini REST API (Retrofit & Serialization)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.serialization)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.okhttp)
}
