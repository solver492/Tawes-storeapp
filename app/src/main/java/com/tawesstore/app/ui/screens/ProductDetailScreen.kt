package com.tawesstore.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.tawesstore.app.data.model.Product
import com.tawesstore.app.data.model.ShoppingMode
import com.tawesstore.app.ui.components.WhatsAppGreen
import com.tawesstore.app.ui.components.openWhatsApp
import com.tawesstore.app.ui.theme.*

@Composable
fun ProductDetailScreen(
    product: Product,
    currentMode: ShoppingMode,
    onBackClick: () -> Unit,
    onAddToCart: (Product, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBackClick() }

    val context = LocalContext.current
    val minQty = if (currentMode == ShoppingMode.WHOLESALE) product.minOrder else 1
    var quantity by remember(product, currentMode) { mutableStateOf(minQty) }

    val unitPrice = if (currentMode == ShoppingMode.WHOLESALE) product.wholesalePrice else product.price
    val totalPrice = unitPrice * quantity

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TawesCream)
    ) {
        // Top App Bar
        Surface(
            color = TawesSurfaceWhite,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.testTag("detail_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Retour",
                        tint = TawesInk
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = product.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TawesInk,
                    maxLines = 1
                )
            }
        }

        // Scrollable content
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Main Product Visual
            Card(
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    AsyncImage(
                        model = product.imageUrl,
                        contentDescription = product.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Discount tag
                    if (currentMode == ShoppingMode.RETAIL && product.discountPercent > 0) {
                        Box(
                            modifier = Modifier
                                .padding(12.dp)
                                .align(Alignment.TopStart)
                                .clip(RoundedCornerShape(6.dp))
                                .background(TawesOrange)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "-${product.discountPercent}% RÉDUCTION",
                                color = TawesSurfaceWhite,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Mode tag
                    Box(
                        modifier = Modifier
                            .padding(12.dp)
                            .align(Alignment.BottomEnd)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (currentMode == ShoppingMode.WHOLESALE) TawesWholesaleBg else TawesMint)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (currentMode == ShoppingMode.WHOLESALE) "Tarif Grossiste" else "Tarif Détail",
                            color = if (currentMode == ShoppingMode.WHOLESALE) TawesWholesaleText else TawesGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Category & Title
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = product.category.uppercase(),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TawesOrange,
                    letterSpacing = 1.sp
                )
                if (product.gender != "tous" && product.gender.isNotEmpty()) {
                    Surface(
                        color = TawesMint,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Section " + product.gender.replaceFirstChar { it.uppercase() },
                            color = TawesGreen,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = product.name,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TawesInk
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Rating & Stock
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    repeat(5) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = null,
                            tint = TawesGold,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "4.9 (38 avis clients)",
                        fontSize = 11.sp,
                        color = TawesMuted
                    )
                }

                Text(
                    text = "En stock (${product.stock} dispo)",
                    fontSize = 11.sp,
                    color = TawesGreen,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Pricing Card
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = TawesSurfaceWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (currentMode == ShoppingMode.WHOLESALE) "Prix unitaire grossiste :" else "Prix unitaire :",
                                fontSize = 11.sp,
                                color = TawesMuted
                            )
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = "${unitPrice.toInt()} DH",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (currentMode == ShoppingMode.WHOLESALE) TawesWholesaleText else TawesGreen
                                )
                                Text(
                                    text = " / ${product.unit}",
                                    fontSize = 12.sp,
                                    color = TawesMuted,
                                    modifier = Modifier.padding(bottom = 3.dp, start = 2.dp)
                                )
                            }
                        }

                        if (currentMode == ShoppingMode.RETAIL && product.oldPrice > product.price) {
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Ancien prix",
                                    fontSize = 10.sp,
                                    color = TawesMuted
                                )
                                Text(
                                    text = "${product.oldPrice.toInt()} DH",
                                    fontSize = 14.sp,
                                    color = TawesMuted,
                                    textDecoration = TextDecoration.LineThrough
                                )
                            }
                        }
                    }

                    if (currentMode == ShoppingMode.WHOLESALE) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Commande minimum : ${product.minOrder} ${product.unit}(s)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TawesOrangeDark
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Quantity Selector & Total
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = TawesSurfaceWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Quantité à commander :",
                            fontSize = 11.sp,
                            color = TawesMuted
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 6.dp)
                        ) {
                            IconButton(
                                onClick = {
                                    if (quantity > minQty) quantity--
                                },
                                enabled = quantity > minQty,
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(if (quantity > minQty) TawesMint else Color(0xFFF1F5F9))
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Remove,
                                    contentDescription = "Moins",
                                    tint = if (quantity > minQty) TawesGreen else TawesMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            Text(
                                text = "$quantity",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TawesInk,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )

                            IconButton(
                                onClick = { quantity++ },
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(TawesMint)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Add,
                                    contentDescription = "Plus",
                                    tint = TawesGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Total estimé :",
                            fontSize = 11.sp,
                            color = TawesMuted
                        )
                        Text(
                            text = "${totalPrice.toInt()} DH",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TawesInk
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Description
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = TawesSurfaceWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Description du produit",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TawesInk
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = product.description.ifEmpty { "Produit garanti de qualité supérieure sélectionné par Tawes Store. Livraison rapide sur tout le Maroc avec paiement à la réception." },
                        fontSize = 12.sp,
                        color = Color(0xFF4B5563),
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Bottom Action Bar: WhatsApp Order + Add to Cart
        Surface(
            color = TawesSurfaceWhite,
            shadowElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Direct WhatsApp Order
                Button(
                    onClick = {
                        val modeLabel = if (currentMode == ShoppingMode.WHOLESALE) "Gros (Revendeur)" else "Détail"
                        val msg = """
                            Bonjour Tawes Store, je souhaite commander :
                            - Produit : ${product.name}
                            - Mode : $modeLabel
                            - Quantité : $quantity ${product.unit}(s)
                            - Prix unitaire : ${unitPrice.toInt()} DH
                            - Total : ${totalPrice.toInt()} DH
                            Merci de confirmer la disponibilité et les modalités de livraison.
                        """.trimIndent()
                        openWhatsApp(context, "212600000000", msg)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("direct_whatsapp_order_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = WhatsAppGreen,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Chat,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "WhatsApp",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Add to Cart
                Button(
                    onClick = {
                        onAddToCart(product, quantity)
                    },
                    modifier = Modifier
                        .weight(1.3f)
                        .height(48.dp)
                        .testTag("add_to_cart_detail_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TawesGreen,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ShoppingCart,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = if (currentMode == ShoppingMode.WHOLESALE) "Ajouter au devis" else "Ajouter au panier",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
