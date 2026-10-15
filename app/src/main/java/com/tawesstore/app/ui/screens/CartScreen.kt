package com.tawesstore.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.tawesstore.app.data.AppConfig
import com.tawesstore.app.data.model.CartItem
import com.tawesstore.app.ui.components.WhatsAppGreen
import com.tawesstore.app.ui.components.openWhatsApp
import com.tawesstore.app.ui.theme.*

@Composable
fun CartScreen(
    cartItems: List<CartItem>,
    onBackClick: () -> Unit,
    onUpdateQuantity: (String, Boolean, Int) -> Unit,
    onRemoveItem: (String, Boolean) -> Unit,
    onClearCart: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBackClick() }

    val context = LocalContext.current
    val totalAmount = cartItems.sumOf { it.totalPrice }
    val totalItemsCount = cartItems.sumOf { it.quantity }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TawesCream)
    ) {
        // Topbar
        Surface(
            color = TawesSurfaceWhite,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.testTag("cart_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Retour",
                            tint = TawesInk
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Mon Panier & Devis",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = TawesInk
                    )
                }

                if (cartItems.isNotEmpty()) {
                    TextButton(
                        onClick = onClearCart,
                        modifier = Modifier.testTag("clear_cart_button")
                    ) {
                        Text(
                            text = "Vider",
                            fontSize = 12.sp,
                            color = Color.Red.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }

        if (cartItems.isEmpty()) {
            // Empty State
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(TawesMint),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ShoppingBag,
                            contentDescription = null,
                            tint = TawesGreen,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Votre panier est vide",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TawesInk
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Découvrez notre catalogue au détail et nos offres grossistes.",
                        fontSize = 12.sp,
                        color = TawesMuted
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = onBackClick,
                        colors = ButtonDefaults.buttonColors(containerColor = TawesGreen),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Parcourir le catalogue")
                    }
                }
            }
        } else {
            // Items list
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(cartItems, key = { "${it.product.id}_${it.isWholesale}" }) { item ->
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = TawesSurfaceWhite),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("cart_item_${item.product.id}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Product Thumbnail
                            Box(
                                modifier = Modifier
                                    .size(70.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFF1F4F1))
                            ) {
                                AsyncImage(
                                    model = item.product.imageUrl,
                                    contentDescription = item.product.name,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            // Item details
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = item.product.name,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = TawesInk,
                                            maxLines = 1
                                        )
                                        Text(
                                            text = if (item.isWholesale) "Tarif Grossiste · ${item.unitPrice.toInt()} DH/u" else "Tarif Détail · ${item.unitPrice.toInt()} DH",
                                            fontSize = 11.sp,
                                            color = if (item.isWholesale) TawesWholesaleText else TawesMuted
                                        )
                                    }

                                    IconButton(
                                        onClick = { onRemoveItem(item.product.id, item.isWholesale) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Delete,
                                            contentDescription = "Supprimer",
                                            tint = TawesMuted,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Quantity & Item Total
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Quantity control
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0xFFF1F5F9))
                                    ) {
                                        IconButton(
                                            onClick = { onUpdateQuantity(item.product.id, item.isWholesale, -1) },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Remove,
                                                contentDescription = "Diminuer",
                                                modifier = Modifier.size(14.dp),
                                                tint = TawesInk
                                            )
                                        }

                                        Text(
                                            text = "${item.quantity}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TawesInk,
                                            modifier = Modifier.padding(horizontal = 8.dp)
                                        )

                                        IconButton(
                                            onClick = { onUpdateQuantity(item.product.id, item.isWholesale, 1) },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Add,
                                                contentDescription = "Augmenter",
                                                modifier = Modifier.size(14.dp),
                                                tint = TawesInk
                                            )
                                        }
                                    }

                                    Text(
                                        text = "${item.totalPrice.toInt()} DH",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TawesInk
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Checkout Summary Footer
            Surface(
                color = TawesSurfaceWhite,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Articles ($totalItemsCount) :",
                            fontSize = 12.sp,
                            color = TawesMuted
                        )
                        Text(
                            text = "${totalAmount.toInt()} DH",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TawesInk
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Livraison Maroc :",
                            fontSize = 12.sp,
                            color = TawesMuted
                        )
                        Text(
                            text = "Gratuite / Paiement réception",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TawesGreen
                        )
                    }

                    Divider(modifier = Modifier.padding(vertical = 10.dp), color = TawesLine)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Total à régler :",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TawesInk
                        )
                        Text(
                            text = "${totalAmount.toInt()} DH",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TawesGreen
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Finalize order via WhatsApp
                    Button(
                        onClick = {
                            val itemsSummary = cartItems.joinToString("\n") { item ->
                                val modeStr = if (item.isWholesale) "Gros" else "Détail"
                                "• ${item.product.name} (x${item.quantity}) - [${modeStr}] : ${item.totalPrice.toInt()} DH"
                            }
                            val orderMessage = """
                                Bonjour Tawes Store, je valide ma commande :
                                
                                $itemsSummary
                                
                                TOTAL : ${totalAmount.toInt()} DH
                                Mode de paiement : À la livraison
                                
                                Merci de me contacter pour la confirmation et l'adresse de livraison.
                            """.trimIndent()

                            openWhatsApp(context, AppConfig.whatsAppNumber, orderMessage)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("finalize_whatsapp_order_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = WhatsAppGreen,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Chat,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Commander via WhatsApp",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
