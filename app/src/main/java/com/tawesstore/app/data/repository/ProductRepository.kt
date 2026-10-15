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

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products.asStateFlow()

    private val _categories = MutableStateFlow(listOf(DEFAULT_CATEGORY))
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
                _products.value = list
                val names = try {
                    SupabaseClient.fetchCategoryNames()
                } catch (_: Exception) {
                    emptyList()
                }
                _categories.value = listOf(DEFAULT_CATEGORY) + names
            } catch (e: Exception) {
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
    }
}
