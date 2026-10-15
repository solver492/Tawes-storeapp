package com.tawesstore.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tawesstore.app.data.model.Product
import com.tawesstore.app.data.model.ShoppingMode
import com.tawesstore.app.ui.components.HeroBanner
import com.tawesstore.app.ui.components.ProductCard
import com.tawesstore.app.ui.components.WhatsAppGreen
import com.tawesstore.app.ui.theme.*

@Composable
fun HomeScreen(
    products: List<Product>,
    categories: List<String>,
    currentMode: ShoppingMode,
    onProductClick: (Product) -> Unit,
    onAddToCart: (Product) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onOpenWhatsAppSupport: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedSection by remember { mutableStateOf("tous") }
    var selectedCategory by remember { mutableStateOf("Tous") }

    val sections = listOf(
        "tous" to "Tout",
        "femme" to "Femme",
        "homme" to "Homme",
        "enfant" to "Enfant"
    )

    val filteredProducts = remember(products, searchQuery, selectedSection, selectedCategory) {
        products.filter { p ->
            val matchesSearch = searchQuery.isEmpty() ||
                    p.name.contains(searchQuery, ignoreCase = true) ||
                    p.category.contains(searchQuery, ignoreCase = true) ||
                    p.description.contains(searchQuery, ignoreCase = true)

            val matchesSection = selectedSection == "tous" || p.gender == selectedSection || p.gender == "tous"
            val matchesCategory = selectedCategory == "Tous" || p.category.equals(selectedCategory, ignoreCase = true)

            matchesSearch && matchesSection && matchesCategory && p.status == "published"
        }
    }

    Box(modifier = modifier.fillMaxSize().background(TawesCream)) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .testTag("home_products_grid"),
            contentPadding = PaddingValues(bottom = 80.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header: Hero Banner
            item(span = { GridItemSpan(2) }) {
                HeroBanner(currentMode = currentMode)
            }

            // Search Bar
            item(span = { GridItemSpan(2) }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("search_input_field"),
                        placeholder = { Text("Rechercher un produit, une marque...", fontSize = 12.sp) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Search,
                                contentDescription = null,
                                tint = TawesMuted,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(
                                        imageVector = Icons.Filled.Clear,
                                        contentDescription = "Effacer",
                                        tint = TawesMuted,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = TawesSurfaceWhite,
                            unfocusedContainerColor = TawesSurfaceWhite,
                            focusedBorderColor = TawesGreen,
                            unfocusedBorderColor = TawesLine
                        )
                    )
                }
            }

            // Sections Row (Tout / Femme / Homme / Enfant)
            item(span = { GridItemSpan(2) }) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    sections.forEach { (key, label) ->
                        val isSelected = selectedSection == key
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSelected) TawesGreen else TawesSurfaceWhite)
                                .clickable { selectedSection = key }
                                .padding(horizontal = 14.dp, vertical = 7.dp)
                                .testTag("section_filter_$key"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) TawesSurfaceWhite else TawesInk
                            )
                        }
                    }
                }
            }

            // Categories Row Chips
            item(span = { GridItemSpan(2) }) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.forEach { cat ->
                        val isSelected = selectedCategory == cat
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isSelected) TawesOrange else TawesSurfaceWhite)
                                .clickable { selectedCategory = cat }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                .testTag("category_filter_$cat"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = cat,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) TawesSurfaceWhite else TawesMuted
                            )
                        }
                    }
                }
            }

            // Header for product listing
            item(span = { GridItemSpan(2) }) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (currentMode == ShoppingMode.WHOLESALE) "Catalogue Grossiste" else "Nos Produits",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TawesInk
                    )
                    Text(
                        text = "${filteredProducts.size} article(s)",
                        fontSize = 11.sp,
                        color = TawesMuted
                    )
                }
            }

            // Product Cards or Empty state
            if (filteredProducts.isEmpty()) {
                item(span = { GridItemSpan(2) }) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Aucun produit trouvé",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TawesInk
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Essayez un autre mot-clé ou catégorie.",
                                fontSize = 12.sp,
                                color = TawesMuted
                            )
                        }
                    }
                }
            } else {
                items(filteredProducts, key = { it.id }) { product ->
                    Box(modifier = Modifier.padding(horizontal = 4.dp)) {
                        ProductCard(
                            product = product,
                            currentMode = currentMode,
                            onProductClick = { onProductClick(product) },
                            onAddToCart = { onAddToCart(product) },
                            onToggleFavorite = { onToggleFavorite(product.id) }
                        )
                    }
                }
            }
        }

        // Floating WhatsApp action button
        FloatingActionButton(
            onClick = onOpenWhatsAppSupport,
            containerColor = WhatsAppGreen,
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .testTag("floating_whatsapp_button")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Chat,
                    contentDescription = "WhatsApp",
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "WhatsApp",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
