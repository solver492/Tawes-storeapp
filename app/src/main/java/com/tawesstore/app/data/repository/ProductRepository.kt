package com.tawesstore.app.data.repository

import com.tawesstore.app.data.model.CartItem
import com.tawesstore.app.data.model.Product
import com.tawesstore.app.data.remote.SupabaseClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Source unique des donnees de la vitrine : les produits PUBLIES sur Supabase
 * en LECTURE SEULE (meme base que le site web). Le panier et les favoris sont
 * locaux ; la commande se finalise via WhatsApp.
 */
class ProductRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _products = MutableStateFlow<List<Product>>(DEMO_PRODUCTS)
    val products: StateFlow<List<Product>> = _products.asStateFlow()

    private val _categories = MutableStateFlow(listOf(DEFAULT_CATEGORY) + DEMO_CATEGORIES)
    val categories: StateFlow<List<String>> = _categories.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    init {
        refresh()
    }

    /** Recharge les produits publies depuis Supabase. */
    fun refresh() {
        _loading.value = true
        _error.value = null
        scope.launch {
            try {
                val list = SupabaseClient.fetchPublishedProducts()
                if (list.isNotEmpty()) {
                    _products.value = list
                } else if (_products.value.isEmpty()) {
                    _products.value = DEMO_PRODUCTS
                }
                val names = try {
                    SupabaseClient.fetchCategoryNames()
                } catch (_: Exception) {
                    emptyList()
                }
                val productCats = list.map { it.category }.filter { it.isNotBlank() }
                val allCats = (names + productCats).distinct()
                _categories.value = listOf(DEFAULT_CATEGORY) + (if (allCats.isNotEmpty()) allCats else DEMO_CATEGORIES)
            } catch (e: Exception) {
                if (_products.value.isEmpty()) {
                    _products.value = DEMO_PRODUCTS
                }
                _error.value = e.message ?: "Impossible de charger les produits."
            } finally {
                _loading.value = false
            }
        }
    }

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
        _cartItems.value = _cartItems.value.filterNot {
            it.product.id == productId && it.isWholesale == isWholesale
        }
    }

    fun clearCart() {
        _cartItems.value = emptyList()
    }

    private companion object {
        const val DEFAULT_CATEGORY = "Tous"

        val DEMO_CATEGORIES = listOf(
            "Téléphones",
            "Électronique",
            "Mode",
            "Maison",
            "Beauté",
            "Électroménager",
            "Accessoires"
        )

        val DEMO_PRODUCTS = listOf(
            Product(
                id = "demo-1",
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
                description = "Un smartphone élégant, rapide et fluide avec écran AMOLED 120Hz et batterie longue autonomie."
            ),
            Product(
                id = "demo-2",
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
                description = "Son immersif spatialisé, réduction de bruit active et autonomie prolongée."
            ),
            Product(
                id = "demo-3",
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
                description = "Sac résistant à l'eau avec compartiment PC 15.6 pouces et port de charge USB."
            ),
            Product(
                id = "demo-4",
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
                description = "Baskets légères avec semelle amortissante à coussin d'air."
            ),
            Product(
                id = "demo-5",
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
                description = "Formule à base d'huile d'argan pure du Maroc et sérum hydratant."
            ),
            Product(
                id = "demo-6",
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
                description = "Moteur puissant 800 Watts avec bol en verre 1.5L pour smoothies et soupes."
            )
        )
    }
}
