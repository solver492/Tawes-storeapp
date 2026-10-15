package com.tawesstore.app.data.model

data class Product(
    val id: String,
    val name: String,
    val category: String,
    val gender: String = "tous", // femme, homme, enfant, tous
    val price: Double,
    val wholesalePrice: Double,
    val oldPrice: Double = 0.0,
    val unit: String = "pièce",
    val stock: Int = 10,
    val minOrder: Int = 1,
    val imageUrl: String,
    val images: List<String> = emptyList(),
    val videoUrl: String? = null,
    val posterUrl: String? = null,
    val description: String = "",
    val status: String = "published", // published, pending, draft
    val isFavorite: Boolean = false
) {
    val discountPercent: Int
        get() = if (oldPrice > price && oldPrice > 0) {
            (((oldPrice - price) / oldPrice) * 100).toInt()
        } else {
            0
        }
}

enum class ShoppingMode {
    RETAIL, // Achat au détail (pour particuliers)
    WHOLESALE // Achat en gros (pour revendeurs)
}
