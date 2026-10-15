import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

// Valeurs de configuration lues a la compilation.
// Priorite : variable d'environnement > propriete Gradle (-P) > keystore.properties.
// Ce sont des valeurs PUBLIQUES (URL Supabase + cle publishable en lecture seule
// + numero WhatsApp de la boutique). Aucun secret ne doit transiter ici.
fun conf(key: String): String =
    System.getenv(key)
        ?: (project.findProperty(key) as String?)
        ?: ""

val keystorePropsFile = rootProject.file("keystore.properties")
val keystoreProps = Properties().apply {
    if (keystorePropsFile.exists()) keystorePropsFile.inputStream().use { load(it) }
}

android {
    namespace = "com.tawesstore.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.tawesstore.app"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Configuration embarquee (voir data/AppConfig.kt).
        buildConfigField("String", "SUPABASE_URL", "\"${conf("SUPABASE_URL")}\"")
        buildConfigField("String", "SUPABASE_ANON_KEY", "\"${conf("SUPABASE_ANON_KEY")}\"")
        buildConfigField("String", "WHATSAPP_NUMBER", "\"${conf("WHATSAPP_NUMBER")}\"")
    }

    // Signature : on ne code JAMAIS le keystore en dur. S'il existe un
    // keystore.properties (non versionne, cf. .gitignore) on l'utilise pour la
    // release ; sinon la release n'est pas signee (Play App Signing s'en charge
    // a l'upload, ou l'on fournit le fichier au moment de publier).
    signingConfigs {
        create("release") {
            if (keystorePropsFile.exists()) {
                storeFile = file(keystoreProps.getProperty("storeFile"))
                storePassword = keystoreProps.getProperty("storePassword")
                keyAlias = keystoreProps.getProperty("keyAlias")
                keyPassword = keystoreProps.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            if (keystorePropsFile.exists()) {
                signingConfig = signingConfigs.getByName("release")
            }
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            // Signature debug automatique (keystore debug genere par AGP).
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

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.coil.compose)
}
