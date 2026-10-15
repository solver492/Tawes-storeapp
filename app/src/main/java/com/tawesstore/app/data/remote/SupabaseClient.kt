package com.tawesstore.app.data.remote

import com.tawesstore.app.data.AppConfig
import com.tawesstore.app.data.model.Product
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/**
 * Lecture des produits publies sur la meme base Supabase que la vitrine web.
 *
 * Regles :
 *  - LECTURE SEULE, via l'API REST PostgREST de Supabase.
 * - On utilise la cle « publishable » (comme le site), jamais la cle secrete.
 * - On ne lit que les produits publies sur la vitrine (is_published_to_website
 *   = true et active = true), exactement comme le site.
 *
 * Aucune dependance reseau ajoutee : on utilise HttpURLConnection + org.json
 * (fournis par Android) pour rester simple et sans risque de version.
 */
object SupabaseClient {

    private const val PRODUCTS_VIEW = "products_with_category"
    private const val MEDIA_TABLE = "product_media"

    class SupabaseException(message: String) : Exception(message)

    /** Produits publies sur la vitrine, dans le meme ordre que le site (recent d'abord). */
    fun fetchPublishedProducts(): List<Product> {
        val query = "select=*" +
            "&is_published_to_website=eq.true" +
            "&active=eq.true" +
            "&order=created_at.desc" +
            "&limit=100"

        val rows = getJsonArray(PRODUCTS_VIEW, query)
        val media = try {
            groupMediaByProduct(fetchMediaFor(rows))
        } catch (_: Exception) {
            emptyMap()
        }
        return (0 until rows.length()).map { index ->
            normalizeProduct(rows.getJSONObject(index), index, media)
        }
    }

    /** Noms des categories actives, dans l'ordre d'affichage defini en base. */
    fun fetchCategoryNames(): List<String> {
        val rows = getJsonArray("categories", "select=name&is_active=eq.true&order=display_order.asc")
        return (0 until rows.length())
            .mapNotNull { rows.getJSONObject(it).optString("name", "").takeIf { n -> n.isNotBlank() } }
    }

    // ---------- Mise en forme produit (identique a la vitrine web) ----------

    private fun normalizeProduct(
        item: org.json.JSONObject,
        index: Int,
        media: Map<String, List<MediaRow>>,
    ): Product {
        val id = item.opt("id")?.toString()
            ?: item.opt("uuid")?.toString()
            ?: "supabase-$index"

        val rows = media[id].orEmpty()
        val carousel = rows.filter { it.type == "image" }.map { it.url }
        val directVideo = rows.firstOrNull { it.type == "video" }
        val vimeoVideo = rows.firstOrNull { it.type == "vimeo" }
        val video = directVideo ?: vimeoVideo
        val poster = rows.firstOrNull { it.type == "poster" }

        val imagesField = item.optJSONArray("images")
        val directImages = if (imagesField != null && imagesField.length() > 0) {
            (0 until imagesField.length()).mapNotNull { i ->
                imagesField.optString(i).takeIf { it.isNotBlank() }
            }
        } else {
            emptyList()
        }
        val firstImage = directImages.firstOrNull().orEmpty()
        val imageCandidate = listOf(
            item.optString("image_url"),
            item.optString("image"),
            item.optString("thumbnail"),
            firstImage,
        ).firstOrNull { it.isNotBlank() }.orEmpty()

        val allImages = (carousel + directImages).distinct()

        val isLot = item.optBoolean("is_lot", false)

        return Product(
            id = id,
            name = textOr(item.optString("name").ifBlank { item.optString("title") }, "Produit Tawes Store"),
            category = textOr(
                item.optString("category_name").ifBlank {
                    item.optString("category").ifBlank { item.optString("type") }
                },
                "Autres",
            ),
            gender = item.optString("gender").lowercase().ifBlank { "tous" },
            price = numberOr(
                firstNumber(item, "suggested_sale_price", "price", "retail_price", "sale_price", "wholesale_price"),
            ),
            wholesalePrice = numberOr(
                firstNumber(item, "wholesale_price", "lot_unit_price", "bulk_price"),
            ),
            oldPrice = numberOr(firstNumber(item, "old_price", "compare_at_price")),
            unit = if (isLot) "lot" else textOr(item.optString("unit"), "pièce"),
            stock = numberOr(firstNumber(item, "stock", "quantity"), 100).toInt(),
            minOrder = if (isLot) 1 else numberOr(firstNumber(item, "min_order", "minimum_order_quantity"), 1).toInt(),
            imageUrl = poster?.url ?: allImages.firstOrNull() ?: imageCandidate,
            images = if (allImages.isNotEmpty()) allImages else if (imageCandidate.isNotBlank()) listOf(imageCandidate) else emptyList(),
            videoUrl = video?.url,
            posterUrl = poster?.url,
            description = textOr(
                item.optString("description").ifBlank { item.optString("details") },
                "Produit disponible sur Tawes Store.",
            ),
            status = "published",
        )
    }

    private fun firstNumber(item: org.json.JSONObject, vararg keys: String): Double? {
        for (key in keys) {
            if (!item.has(key) || item.isNull(key)) continue
            val value = item.optDouble(key, Double.NaN)
            if (!value.isNaN()) return value
        }
        return null
    }

    private fun numberOr(value: Double?, fallback: Number = 0.0): Double =
        value?.takeIf { !it.isNaN() } ?: fallback.toDouble()

    private fun textOr(value: String, fallback: String): String =
        value.trim().ifBlank { fallback }

    // ---------- Medias ----------

    private data class MediaRow(val productId: String, val type: String, val url: String)

    private fun fetchMediaFor(products: org.json.JSONArray): List<MediaRow> {
        if (products.length() == 0) return emptyList()
        val ids = (0 until products.length())
            .mapNotNull { products.getJSONObject(it).opt("id")?.toString() }
        if (ids.isEmpty()) return emptyList()

        val filter = ids.joinToString(",") { URLEncoder.encode(it, "UTF-8") }
        val rows = getJsonArray(
            MEDIA_TABLE,
            "select=product_id,media_type,media_url,sort_order&product_id=in.($filter)&order=sort_order.asc&limit=1000",
        )
        return (0 until rows.length()).map { i ->
            val row = rows.getJSONObject(i)
            MediaRow(
                productId = row.opt("product_id")?.toString() ?: "",
                type = row.optString("media_type"),
                url = row.optString("media_url"),
            )
        }
    }

    private fun groupMediaByProduct(rows: List<MediaRow>): Map<String, List<MediaRow>> =
        rows.groupBy { it.productId }

    // ---------- HTTP ----------

    private fun getJsonArray(table: String, query: String): org.json.JSONArray {
        val base = AppConfig.supabaseUrl.trimEnd('/')
        val key = AppConfig.supabaseAnonKey
        if (!AppConfig.isSupabaseConfigured) {
            throw SupabaseException("Supabase non configure (SUPABASE_URL / SUPABASE_ANON_KEY).")
        }

        val url = URL("$base/rest/v1/$table?$query")
        val connection = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 15000
            readTimeout = 15000
            setRequestProperty("apikey", key)
            setRequestProperty("Authorization", "Bearer $key")
            setRequestProperty("Accept", "application/json")
        }

        try {
            val status = connection.responseCode
            if (status !in 200..299) {
                throw SupabaseException("Supabase a repondu $status.")
            }
            val body = connection.inputStream.bufferedReader().use { it.readText() }
            return org.json.JSONArray(body)
        } catch (e: SupabaseException) {
            throw e
        } catch (e: Exception) {
            throw SupabaseException(e.message ?: "Erreur reseau vers Supabase.")
        } finally {
            connection.disconnect()
        }
    }
}
