package com.tawesstore.app.data.repository

import com.tawesstore.app.data.model.CartItem
import com.tawesstore.app.data.model.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

class ProductRepository {

    private val _products = MutableStateFlow<List<Product>>(getInitialProducts())
    val products: StateFlow<List<Product>> = _products.asStateFlow()

    private val _pendingQueue = MutableStateFlow<List<Product>>(getInitialPendingProducts())
    val pendingQueue: StateFlow<List<Product>> = _pendingQueue.asStateFlow()

    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    val cartCount: StateFlow<Int> = MutableStateFlow(0).also { flow ->
        // will keep sync
    }

    val categories: List<String> = listOf(
        "Tous",
        "Téléphones",
        "Électronique",
        "Mode",
        "Maison",
        "Beauté",
        "Électroménager",
        "Accessoires"
    )

    fun toggleFavorite(productId: String) {
        _products.value = _products.value.map {
            if (it.id == productId) it.copy(isFavorite = !it.isFavorite) else it
        }
    }

    fun addToCart(product: Product, quantity: Int, isWholesale: Boolean) {
        val current = _cartItems.value.toMutableList()
        val index = current.indexOfFirst { it.product.id == product.id && it.isWholesale == isWholesale }
        if (index >= 0) {
            val existing = current[index]
            current[index] = existing.copy(quantity = existing.quantity + quantity)
        } else {
            current.add(CartItem(product = product, quantity = quantity, isWholesale = isWholesale))
        }
        _cartItems.value = current
    }

    fun updateCartQuantity(productId: String, isWholesale: Boolean, delta: Int) {
        val current = _cartItems.value.toMutableList()
        val index = current.indexOfFirst { it.product.id == productId && it.isWholesale == isWholesale }
        if (index >= 0) {
            val item = current[index]
            val newQty = item.quantity + delta
            val min = if (isWholesale) item.product.minOrder else 1
            if (newQty < min) {
                current.removeAt(index)
            } else {
                current[index] = item.copy(quantity = newQty)
            }
            _cartItems.value = current
        }
    }

    fun removeFromCart(productId: String, isWholesale: Boolean) {
        _cartItems.value = _cartItems.value.filterNot { it.product.id == productId && it.isWholesale == isWholesale }
    }

    fun clearCart() {
        _cartItems.value = emptyList()
    }

    // --- Backoffice Actions ---
    fun publishPendingProduct(
        productId: String,
        name: String,
        category: String,
        gender: String,
        price: Double,
        wholesalePrice: Double,
        stock: Int,
        description: String
    ) {
        val pending = _pendingQueue.value.find { it.id == productId }
        if (pending != null) {
            val published = pending.copy(
                name = name,
                category = category,
                gender = gender,
                price = price,
                wholesalePrice = wholesalePrice,
                stock = stock,
                description = description,
                status = "published"
            )
            _pendingQueue.value = _pendingQueue.value.filterNot { it.id == productId }
            _products.value = listOf(published) + _products.value
        }
    }

    fun rejectPendingProduct(productId: String) {
        _pendingQueue.value = _pendingQueue.value.filterNot { it.id == productId }
    }

    fun addManualProduct(product: Product) {
        _products.value = listOf(product) + _products.value
    }

    fun updateProduct(product: Product) {
        _products.value = _products.value.map {
            if (it.id == product.id) product else it
        }
    }

    fun deleteProduct(productId: String) {
        _products.value = _products.value.filterNot { it.id == productId }
    }

    companion object {
        private fun getInitialProducts(): List<Product> = listOf(
            Product(
                id = "p-1",
                name = "Smartphone Nova X 5G",
                category = "Téléphones",
                gender = "tous",
                price = 2499.0,
                wholesalePrice = 2199.0,
                oldPrice = 2899.0,
                unit = "pièce",
                stock = 42,
                minOrder = 1,
                imageUrl = "https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?auto=format&fit=crop&w=900&q=85",
                images = listOf(
                    "https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?auto=format&fit=crop&w=900&q=85",
                    "https://images.unsplash.com/photo-1598327105666-5b89351aff97?auto=format&fit=crop&w=900&q=85"
                ),
                description = "Un smartphone élégant, rapide et fluide avec écran AMOLED 120Hz et triple capteur photo haute définition. Batterie 5000 mAh longue autonomie."
            ),
            Product(
                id = "p-2",
                name = "Casque sans fil Pulse ANC",
                category = "Électronique",
                gender = "tous",
                price = 349.0,
                wholesalePrice = 289.0,
                oldPrice = 499.0,
                unit = "pièce",
                stock = 86,
                minOrder = 5,
                imageUrl = "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?auto=format&fit=crop&w=900&q=85",
                images = listOf(
                    "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?auto=format&fit=crop&w=900&q=85",
                    "https://images.unsplash.com/photo-1484704849700-f032a568e944?auto=format&fit=crop&w=900&q=85"
                ),
                description = "Son immersif spatialisé, réduction de bruit active (ANC) et jusqu'à 40 heures d'écoute continue. Coussins mémoire de forme ultra confortables."
            ),
            Product(
                id = "p-3",
                name = "Sac à dos Urban Étanche",
                category = "Mode",
                gender = "homme",
                price = 299.0,
                wholesalePrice = 229.0,
                oldPrice = 399.0,
                unit = "pièce",
                stock = 64,
                minOrder = 10,
                imageUrl = "https://images.unsplash.com/photo-1553062407-98eeb64c6a62?auto=format&fit=crop&w=900&q=85",
                description = "Sac ergonomique résistant à l'eau avec compartiment pour PC portable 15.6 pouces, poches antivol et port de charge USB externe."
            ),
            Product(
                id = "p-4",
                name = "Sneakers Street One Pro",
                category = "Mode",
                gender = "femme",
                price = 549.0,
                wholesalePrice = 449.0,
                oldPrice = 749.0,
                unit = "paire",
                stock = 47,
                minOrder = 6,
                imageUrl = "https://images.unsplash.com/photo-1542291026-7eec264c27ff?auto=format&fit=crop&w=900&q=85",
                description = "Baskets légères avec semelle amortissante à coussin d'air. Confort exceptionnel et design contemporain pour le sport et le quotidien."
            ),
            Product(
                id = "p-5",
                name = "Pack Soins Visage Bio Essentiel",
                category = "Beauté",
                gender = "femme",
                price = 459.0,
                wholesalePrice = 379.0,
                oldPrice = 599.0,
                unit = "pack",
                stock = 25,
                minOrder = 6,
                imageUrl = "https://images.unsplash.com/photo-1556228578-8c89e6adf883?auto=format&fit=crop&w=900&q=85",
                description = "Formule à base d'huile d'argan pure du Maroc, sérum hydratant à l'acide hyaluronique et crème nourrissante régénérante jour et nuit."
            ),
            Product(
                id = "p-6",
                name = "Mixeur Kitchen Pro 800W",
                category = "Électroménager",
                gender = "tous",
                price = 699.0,
                wholesalePrice = 579.0,
                oldPrice = 899.0,
                unit = "pièce",
                stock = 18,
                minOrder = 3,
                imageUrl = "https://images.unsplash.com/photo-1570222094114-d054a817e56b?auto=format&fit=crop&w=900&q=85",
                description = "Moteur puissant 800 Watts avec 6 lames en acier inoxydable, bol en verre thermique 1.5L et réglage variateur continu pour smoothies et soupes."
            ),
            Product(
                id = "p-7",
                name = "Lampe LED Studio Tactile",
                category = "Maison",
                gender = "tous",
                price = 179.0,
                wholesalePrice = 139.0,
                oldPrice = 249.0,
                unit = "pièce",
                stock = 31,
                minOrder = 8,
                imageUrl = "https://images.unsplash.com/photo-1507473885765-e6ed057f782c?auto=format&fit=crop&w=900&q=85",
                description = "Lampe de bureau moderne avec 3 teintes de lumière (chaude, neutre, blanche), luminosité variable et socle de charge sans fil pour smartphone."
            ),
            Product(
                id = "p-8",
                name = "Ensemble Sport Enfant Respirant",
                category = "Mode",
                gender = "enfant",
                price = 220.0,
                wholesalePrice = 160.0,
                oldPrice = 299.0,
                unit = "lot",
                stock = 35,
                minOrder = 10,
                imageUrl = "https://images.unsplash.com/photo-1519238263530-99bdd11df2ea?auto=format&fit=crop&w=900&q=85",
                description = "Ensemble 2 pièces t-shirt et short en tissu technique respirant séchage rapide. Couleurs vives et finitions soignées pour garçons et filles."
            )
        )

        private fun getInitialPendingProducts(): List<Product> = listOf(
            Product(
                id = "tg-101",
                name = "Montre Chrono Sport Cuir",
                category = "Accessoires",
                gender = "homme",
                price = 399.0,
                wholesalePrice = 280.0,
                unit = "pièce",
                stock = 50,
                minOrder = 5,
                imageUrl = "https://images.unsplash.com/photo-1523275335684-37898b6baf30?auto=format&fit=crop&w=800&q=85",
                description = "Détecté sur Telegram. Cadran chronographe avec bracelet en cuir véritable résistant à l'eau 3 ATM.",
                status = "pending",
                sourceChannel = "Grossiste Derb Ghallef Horlogerie"
            ),
            Product(
                id = "tg-102",
                name = "Robe d'été Fleurie Soie",
                category = "Mode",
                gender = "femme",
                price = 280.0,
                wholesalePrice = 190.0,
                unit = "pièce",
                stock = 30,
                minOrder = 8,
                imageUrl = "https://images.unsplash.com/photo-1496747611176-843222e1e57c?auto=format&fit=crop&w=800&q=85",
                description = "Arrivage textile Turquie. Tissu fluide satiné motifs floraux taille S à XL.",
                status = "pending",
                sourceChannel = "Fashion Import Casablanca"
            ),
            Product(
                id = "tg-103",
                name = "Écouteurs TWS Pro Gaming",
                category = "Électronique",
                gender = "tous",
                price = 199.0,
                wholesalePrice = 135.0,
                unit = "pièce",
                stock = 100,
                minOrder = 12,
                imageUrl = "https://images.unsplash.com/photo-1590658268037-6bf12165a8df?auto=format&fit=crop&w=800&q=85",
                description = "Faible latence 45ms, éclairage LED dynamique, boîtier de charge rapide Type-C.",
                status = "pending",
                sourceChannel = "Tech Direct Grossiste"
            )
        )
    }
}
