import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

// Valeurs de configuration lues a la compilation.
// Priorite : variable d'environnement > propriete Gradle (-P) > valeur par defaut.
//
// Ce sont des valeurs PUBLIQUES (URL Supabase + cle publishable en LECTURE seule
// + numero WhatsApp de la boutique). La cle publishable est deja servie aux
// navigateurs par le site pubstack.space : elle n'est pas secrete. Ne JAMAIS
// mettre la cle secrete (sb_secret_…) ici.
val defaultSupabaseUrl = "https://nfoefhwmgjatbqyibclp.supabase.co"
val defaultSupabaseAnonKey = "sb_publishable_MqgDM0Iyhrr4RCw1xEOr0g_2mg62du7"
val defaultWhatsAppNumber = "212657039145"

fun conf(key: String, default: String): String {
    val env = System.getenv(key)?.trim()
    if (!env.isNullOrBlank() && !env.contains("VOTRE-PROJET") && !env.contains("YOUR-") && !env.contains("example.com")) {
        return env
    }
    val prop = (project.findProperty(key) as String?)?.trim()
    if (!prop.isNullOrBlank() && !prop.contains("VOTRE-PROJET") && !prop.contains("YOUR-") && !prop.contains("example.com")) {
        return prop
    }
    return default
}

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
        buildConfigField("String", "SUPABASE_URL", "\"${conf("SUPABASE_URL", defaultSupabaseUrl)}\"")
        buildConfigField("String", "SUPABASE_ANON_KEY", "\"${conf("SUPABASE_ANON_KEY", defaultSupabaseAnonKey)}\"")
        buildConfigField("String", "WHATSAPP_NUMBER", "\"${conf("WHATSAPP_NUMBER", defaultWhatsAppNumber)}\"")
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
