package com.tawesstore.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.tawesstore.app.data.model.Product
import com.tawesstore.app.ui.theme.*

@Composable
fun BackofficeScreen(
    pendingQueue: List<Product>,
    allProducts: List<Product>,
    categories: List<String>,
    onBackClick: () -> Unit,
    onPublishPending: (String, String, String, String, Double, Double, Int, String) -> Unit,
    onRejectPending: (String) -> Unit,
    onAddProduct: (Product) -> Unit,
    onDeleteProduct: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBackClick() }

    var selectedTab by remember { mutableIntStateOf(0) } // 0: File d'attente (Telegram), 1: Produits, 2: Nouveau

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TawesCream)
    ) {
        // Topbar
        Surface(
            color = TawesInk,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.testTag("backoffice_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Retour",
                            tint = TawesCream
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Tawes",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = TawesCream
                    )
                    Text(
                        text = " · Gestion",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Normal,
                        color = TawesOrangeLight
                    )
                }

                // Tab Row
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = TawesInk,
                    contentColor = TawesSurfaceWhite
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("File Telegram", fontSize = 11.sp)
                                if (pendingQueue.isNotEmpty()) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Badge(containerColor = TawesOrange) {
                                        Text("${pendingQueue.size}", fontSize = 9.sp)
                                    }
                                }
                            }
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Catalogue (${allProducts.size})", fontSize = 11.sp) }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("+ Ajouter", fontSize = 11.sp) }
                    )
                }
            }
        }

        // Content
        when (selectedTab) {
            0 -> PendingQueueView(
                pendingQueue = pendingQueue,
                categories = categories,
                onPublish = onPublishPending,
                onReject = onRejectPending
            )
            1 -> ProductsCatalogView(
                products = allProducts,
                onDelete = onDeleteProduct
            )
            2 -> AddProductView(
                categories = categories,
                onAdd = { newProd ->
                    onAddProduct(newProd)
                    selectedTab = 1
                }
            )
        }
    }
}

@Composable
private fun PendingQueueView(
    pendingQueue: List<Product>,
    categories: List<String>,
    onPublish: (String, String, String, String, Double, Double, Int, String) -> Unit,
    onReject: (String) -> Unit
) {
    if (pendingQueue.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Filled.Inbox,
                    contentDescription = null,
                    tint = TawesMuted,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "File d'attente vide",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TawesInk
                )
                Text(
                    text = "Aucune nouvelle publication Telegram en attente de validation.",
                    fontSize = 12.sp,
                    color = TawesMuted
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(pendingQueue, key = { it.id }) { item ->
                var name by remember(item) { mutableStateOf(item.name) }
                var retailPriceStr by remember(item) { mutableStateOf("${item.price.toInt()}") }
                var wholesalePriceStr by remember(item) { mutableStateOf("${item.wholesalePrice.toInt()}") }
                var selectedCategory by remember(item) { mutableStateOf(item.category) }
                var selectedGender by remember(item) { mutableStateOf(item.gender) }
                var stockStr by remember(item) { mutableStateOf("${item.stock}") }

                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = TawesSurfaceWhite),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("pending_item_${item.id}")
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFF1F4F1))
                            ) {
                                AsyncImage(
                                    model = item.imageUrl,
                                    contentDescription = item.name,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.sourceChannel ?: "Canal Telegram",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TawesOrange
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                OutlinedTextField(
                                    value = name,
                                    onValueChange = { name = it },
                                    label = { Text("Nom du produit", fontSize = 10.sp) },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Price and Margin Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = wholesalePriceStr,
                                onValueChange = { wholesalePriceStr = it },
                                label = { Text("Prix Gros (DH)", fontSize = 10.sp) },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = retailPriceStr,
                                onValueChange = { retailPriceStr = it },
                                label = { Text("Prix Vente (DH)", fontSize = 10.sp) },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            val wholesaleVal = wholesalePriceStr.toDoubleOrNull() ?: 0.0
                            val retailVal = retailPriceStr.toDoubleOrNull() ?: 0.0
                            val margin = retailVal - wholesaleVal

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(TawesMint)
                                    .padding(6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Marge", fontSize = 9.sp, color = TawesGreen)
                                    Text("+${margin.toInt()} DH", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TawesGreen)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Category and Section selectors
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = selectedCategory,
                                onValueChange = { selectedCategory = it },
                                label = { Text("Catégorie", fontSize = 10.sp) },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = selectedGender,
                                onValueChange = { selectedGender = it },
                                label = { Text("Section (homme/femme)", fontSize = 10.sp) },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Action Buttons: Valider & Publier vs Rejeter
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(
                                onClick = { onReject(item.id) },
                                colors = ButtonDefaults.textButtonColors(contentColor = Color.Red.copy(alpha = 0.8f))
                            ) {
                                Icon(imageVector = Icons.Filled.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Rejeter")
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Button(
                                onClick = {
                                    val finalRetail = retailPriceStr.toDoubleOrNull() ?: item.price
                                    val finalWholesale = wholesalePriceStr.toDoubleOrNull() ?: item.wholesalePrice
                                    val finalStock = stockStr.toIntOrNull() ?: 20
                                    onPublish(
                                        item.id,
                                        name,
                                        selectedCategory,
                                        selectedGender,
                                        finalRetail,
                                        finalWholesale,
                                        finalStock,
                                        item.description
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = TawesGreen),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(imageVector = Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Valider & Publier")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProductsCatalogView(
    products: List<Product>,
    onDelete: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(products, key = { it.id }) { item ->
            Card(
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = TawesSurfaceWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFF1F4F1))
                    ) {
                        AsyncImage(
                            model = item.imageUrl,
                            contentDescription = item.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.name,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TawesInk,
                            maxLines = 1
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Gros: ${item.wholesalePrice.toInt()} DH",
                                fontSize = 11.sp,
                                color = TawesWholesaleText,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Vente: ${item.price.toInt()} DH",
                                fontSize = 11.sp,
                                color = TawesGreen,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "Catégorie : ${item.category} · Stock : ${item.stock}",
                            fontSize = 10.sp,
                            color = TawesMuted
                        )
                    }

                    IconButton(onClick = { onDelete(item.id) }) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Supprimer",
                            tint = TawesMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AddProductView(
    categories: List<String>,
    onAdd: (Product) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(categories.getOrElse(1) { "Mode" }) }
    var gender by remember { mutableStateOf("tous") }
    var priceStr by remember { mutableStateOf("299") }
    var wholesalePriceStr by remember { mutableStateOf("219") }
    var minOrderStr by remember { mutableStateOf("1") }
    var stockStr by remember { mutableStateOf("25") }
    var imageUrl by remember { mutableStateOf("https://images.unsplash.com/photo-1523275335684-37898b6baf30?auto=format&fit=crop&w=800&q=85") }
    var description by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Ajouter un nouveau produit",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TawesInk
            )
            Text(
                text = "Renseignez les détails pour le publier directement dans le catalogue.",
                fontSize = 11.sp,
                color = TawesMuted
            )
        }

        item {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nom du produit") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Catégorie") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = gender,
                    onValueChange = { gender = it },
                    label = { Text("Section") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = wholesalePriceStr,
                    onValueChange = { wholesalePriceStr = it },
                    label = { Text("Prix Gros (DH)") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = priceStr,
                    onValueChange = { priceStr = it },
                    label = { Text("Prix Vente (DH)") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = stockStr,
                    onValueChange = { stockStr = it },
                    label = { Text("Stock initial") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = minOrderStr,
                    onValueChange = { minOrderStr = it },
                    label = { Text("Min commande gros") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }
        }

        item {
            OutlinedTextField(
                value = imageUrl,
                onValueChange = { imageUrl = it },
                label = { Text("URL de l'image") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }

        item {
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )
        }

        item {
            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = {
                    if (name.isNotEmpty()) {
                        val product = Product(
                            id = "manual-${System.currentTimeMillis()}",
                            name = name,
                            category = category,
                            gender = gender,
                            price = priceStr.toDoubleOrNull() ?: 100.0,
                            wholesalePrice = wholesalePriceStr.toDoubleOrNull() ?: 80.0,
                            stock = stockStr.toIntOrNull() ?: 10,
                            minOrder = minOrderStr.toIntOrNull() ?: 1,
                            imageUrl = imageUrl,
                            description = description,
                            status = "published"
                        )
                        onAdd(product)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("submit_new_product_button"),
                colors = ButtonDefaults.buttonColors(containerColor = TawesGreen),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(imageVector = Icons.Filled.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Enregistrer et publier", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
