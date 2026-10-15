package com.tawesstore.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tawesstore.app.data.model.Product
import com.tawesstore.app.data.model.ShoppingMode
import com.tawesstore.app.data.repository.ProductRepository
import com.tawesstore.app.ui.components.TawesTopBar
import com.tawesstore.app.ui.components.WhatsAppSupportDialog
import com.tawesstore.app.ui.screens.BackofficeScreen
import com.tawesstore.app.ui.screens.CartScreen
import com.tawesstore.app.ui.screens.HomeScreen
import com.tawesstore.app.ui.screens.ProductDetailScreen
import com.tawesstore.app.ui.theme.TawesStoreTheme
import kotlinx.coroutines.launch

enum class Screen {
    HOME,
    DETAIL,
    CART,
    BACKOFFICE
}

class MainActivity : ComponentActivity() {

    private val repository = ProductRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            TawesStoreTheme {
                val products by repository.products.collectAsStateWithLifecycle()
                val pendingQueue by repository.pendingQueue.collectAsStateWithLifecycle()
                val cartItems by repository.cartItems.collectAsStateWithLifecycle()

                var currentScreen by remember { mutableStateOf(Screen.HOME) }
                var selectedProduct by remember { mutableStateOf<Product?>(null) }
                var currentMode by remember { mutableStateOf(ShoppingMode.RETAIL) }
                var showWhatsAppDialog by remember { mutableStateOf(false) }

                val snackbarHostState = remember { SnackbarHostState() }
                val coroutineScope = rememberCoroutineScope()

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    topBar = {
                        if (currentScreen == Screen.HOME) {
                            TawesTopBar(
                                currentMode = currentMode,
                                onModeChange = { currentMode = it },
                                cartItemCount = cartItems.sumOf { it.quantity },
                                onCartClick = { currentScreen = Screen.CART },
                                isBackofficeActive = false,
                                onBackofficeToggle = { currentScreen = Screen.BACKOFFICE }
                            )
                        }
                    }
                ) { innerPadding ->
                    val screenModifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)

                    when (currentScreen) {
                        Screen.HOME -> {
                            HomeScreen(
                                products = products,
                                categories = repository.categories,
                                currentMode = currentMode,
                                onProductClick = { product ->
                                    selectedProduct = product
                                    currentScreen = Screen.DETAIL
                                },
                                onAddToCart = { product ->
                                    val isWholesale = currentMode == ShoppingMode.WHOLESALE
                                    val qty = if (isWholesale) product.minOrder else 1
                                    repository.addToCart(product, qty, isWholesale)
                                    coroutineScope.launch {
                                        val label = if (isWholesale) "Ajouté au devis (${qty} ${product.unit}s)" else "Ajouté au panier"
                                        snackbarHostState.showSnackbar(
                                            message = "${product.name} - $label",
                                            duration = SnackbarDuration.Short
                                        )
                                    }
                                },
                                onToggleFavorite = { productId ->
                                    repository.toggleFavorite(productId)
                                },
                                onOpenWhatsAppSupport = {
                                    showWhatsAppDialog = true
                                },
                                modifier = screenModifier
                            )
                        }

                        Screen.DETAIL -> {
                            selectedProduct?.let { product ->
                                ProductDetailScreen(
                                    product = product,
                                    currentMode = currentMode,
                                    onBackClick = { currentScreen = Screen.HOME },
                                    onAddToCart = { prod, qty ->
                                        repository.addToCart(prod, qty, currentMode == ShoppingMode.WHOLESALE)
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar(
                                                message = "${prod.name} ($qty) ajouté au panier !",
                                                duration = SnackbarDuration.Short
                                            )
                                        }
                                        currentScreen = Screen.CART
                                    },
                                    modifier = screenModifier
                                )
                            } ?: run {
                                currentScreen = Screen.HOME
                            }
                        }

                        Screen.CART -> {
                            CartScreen(
                                cartItems = cartItems,
                                onBackClick = { currentScreen = Screen.HOME },
                                onUpdateQuantity = { id, isWholesale, delta ->
                                    repository.updateCartQuantity(id, isWholesale, delta)
                                },
                                onRemoveItem = { id, isWholesale ->
                                    repository.removeFromCart(id, isWholesale)
                                },
                                onClearCart = {
                                    repository.clearCart()
                                },
                                modifier = screenModifier
                            )
                        }

                        Screen.BACKOFFICE -> {
                            BackofficeScreen(
                                pendingQueue = pendingQueue,
                                allProducts = products,
                                categories = repository.categories,
                                onBackClick = { currentScreen = Screen.HOME },
                                onPublishPending = { id, name, cat, gender, price, wholesale, stock, desc ->
                                    repository.publishPendingProduct(
                                        productId = id,
                                        name = name,
                                        category = cat,
                                        gender = gender,
                                        price = price,
                                        wholesalePrice = wholesale,
                                        stock = stock,
                                        description = desc
                                    )
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("Produit publié avec succès dans la vitrine !")
                                    }
                                },
                                onRejectPending = { id ->
                                    repository.rejectPendingProduct(id)
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("Publication Telegram rejetée.")
                                    }
                                },
                                onAddProduct = { newProd ->
                                    repository.addManualProduct(newProd)
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("Nouveau produit ajouté au catalogue !")
                                    }
                                },
                                onDeleteProduct = { id ->
                                    repository.deleteProduct(id)
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("Produit supprimé du catalogue.")
                                    }
                                },
                                modifier = screenModifier
                            )
                        }
                    }

                    if (showWhatsAppDialog) {
                        WhatsAppSupportDialog(
                            onDismiss = { showWhatsAppDialog = false }
                        )
                    }
                }
            }
        }
    }
}
