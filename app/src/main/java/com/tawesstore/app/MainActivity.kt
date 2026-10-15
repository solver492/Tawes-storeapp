package com.tawesstore.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tawesstore.app.data.model.Product
import com.tawesstore.app.data.model.ShoppingMode
import com.tawesstore.app.data.repository.ProductRepository
import com.tawesstore.app.ui.components.TawesTopBar
import com.tawesstore.app.ui.components.WhatsAppSupportDialog
import com.tawesstore.app.ui.screens.CartScreen
import com.tawesstore.app.ui.screens.HomeScreen
import com.tawesstore.app.ui.screens.ProductDetailScreen
import com.tawesstore.app.ui.theme.TawesMuted
import com.tawesstore.app.ui.theme.TawesStoreTheme
import kotlinx.coroutines.launch

enum class Screen {
    HOME,
    DETAIL,
    CART
}

class MainActivity : ComponentActivity() {

    private val repository = ProductRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            TawesStoreTheme {
                val products by repository.products.collectAsStateWithLifecycle()
                val categories by repository.categories.collectAsStateWithLifecycle()
                val cartItems by repository.cartItems.collectAsStateWithLifecycle()
                val loading by repository.loading.collectAsStateWithLifecycle()
                val error by repository.error.collectAsStateWithLifecycle()

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
                                onCartClick = { currentScreen = Screen.CART }
                            )
                        }
                    }
                ) { innerPadding ->
                    val screenModifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)

                    when (currentScreen) {
                        Screen.HOME -> {
                            if (products.isEmpty() && (loading || error != null)) {
                                LoadingOrError(
                                    loading = loading,
                                    error = error,
                                    onRetry = { repository.refresh() },
                                    modifier = screenModifier
                                )
                            } else {
                                HomeScreen(
                                    products = products,
                                    categories = categories,
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

/** Etat de chargement / erreur initial, quand aucun produit n'est encore affiche. */
@Composable
private fun LoadingOrError(
    loading: Boolean,
    error: String?,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        if (loading) {
            CircularProgressIndicator()
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = "Impossible de charger la boutique",
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = error ?: "Vérifiez votre connexion puis réessayez.",
                    fontSize = 12.sp,
                    color = TawesMuted,
                    textAlign = TextAlign.Center
                )
                Button(onClick = onRetry) {
                    Text("Réessayer")
                }
            }
        }
    }
}
