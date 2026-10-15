package com.tawesstore.app.data

import com.tawesstore.app.BuildConfig

/**
 * Configuration de l'application, injectee a la compilation (voir
 * app/build.gradle.kts). Ces valeurs sont PUBLIQUES : l'URL Supabase, la cle
 * « publishable » (lecture seule, comme le site vitrine) et le numero WhatsApp
 * de la boutique. Aucun secret, aucune cle d'administration.
 */
object AppConfig {

    val supabaseUrl: String = BuildConfig.SUPABASE_URL.trim()

    /** Cle publishable/anon. Donne UNIQUEMENT acces en lecture aux produits publies (RLS). */
    val supabaseAnonKey: String = BuildConfig.SUPABASE_ANON_KEY.trim()

    /** Numero WhatsApp de la boutique, format international sans « + ». */
    val whatsAppNumber: String =
        BuildConfig.WHATSAPP_NUMBER.trim().ifBlank { "212600000000" }

    /** Vrai si Supabase est renseigne ; sinon l'ecran affiche un message clair. */
    val isSupabaseConfigured: Boolean
        get() = supabaseUrl.startsWith("http") && supabaseAnonKey.isNotBlank()
}
