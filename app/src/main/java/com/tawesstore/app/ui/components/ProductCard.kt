package com.tawesstore.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.tawesstore.app.data.model.Product
import com.tawesstore.app.data.model.ShoppingMode
import com.tawesstore.app.ui.theme.*

@Composable
fun ProductCard(
    product: Product,
    currentMode: ShoppingMode,
    onProductClick: () -> Unit,
    onAddToCart: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable { onProductClick() }
            .testTag("product_card_${product.id}"),
        colors = CardDefaults.cardColors(containerColor = TawesSurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Visual + tags
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .background(Color(0xFFF1F4F1))
            ) {
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
                            .padding(8.dp)
                            .align(Alignment.TopStart)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFFFFF0E5))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "-${product.discountPercent}%",
                            color = TawesOrangeDark,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Section tag if exists
                if (product.gender != "tous" && product.gender.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .padding(8.dp)
                            .align(Alignment.BottomStart)
                            .clip(RoundedCornerShape(4.dp))
                            .background(TawesGreen.copy(alpha = 0.85f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = product.gender.replaceFirstChar { it.uppercase() },
                            color = TawesSurfaceWhite,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Favorite Heart Button
                Box(
                    modifier = Modifier
                        .padding(8.dp)
                        .size(28.dp)
                        .align(Alignment.TopEnd)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.9f))
                        .clickable { onToggleFavorite() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (product.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = "Favori",
                        tint = if (product.isFavorite) TawesOrange else TawesGreen,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Info section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
            ) {
                // Category
                Text(
                    text = product.category.uppercase(),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = TawesOrange,
                    letterSpacing = 0.8.sp
                )

                Spacer(modifier = Modifier.height(2.dp))

                // Product Name
                Text(
                    text = product.name,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TawesInk,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 16.sp,
                    modifier = Modifier.height(34.dp)
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Rating
                Row(verticalAlignment = Alignment.CenterVertically) {
                    repeat(5) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = null,
                            tint = TawesGold,
                            modifier = Modifier.size(11.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "(4.9)",
                        fontSize = 10.sp,
                        color = TawesMuted
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Price display based on mode
                val displayPrice = if (currentMode == ShoppingMode.WHOLESALE) product.wholesalePrice else product.price
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "${displayPrice.toInt()} DH",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (currentMode == ShoppingMode.WHOLESALE) TawesWholesaleText else TawesInk
                    )
                    if (currentMode == ShoppingMode.RETAIL && product.oldPrice > product.price) {
                        Text(
                            text = "${product.oldPrice.toInt()} DH",
                            fontSize = 10.sp,
                            color = TawesMuted,
                            textDecoration = TextDecoration.LineThrough
                        )
                    }
                }

                if (currentMode == ShoppingMode.WHOLESALE) {
                    Text(
                        text = "Min. ${product.minOrder} ${product.unit}(s)",
                        fontSize = 10.sp,
                        color = TawesWholesaleText,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Add to Cart / Devis button
                Button(
                    onClick = onAddToCart,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(34.dp)
                        .testTag("add_to_cart_btn_${product.id}"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (currentMode == ShoppingMode.WHOLESALE) TawesWholesaleBg else TawesMint,
                        contentColor = if (currentMode == ShoppingMode.WHOLESALE) TawesWholesaleText else TawesGreen
                    ),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (currentMode == ShoppingMode.WHOLESALE) "Au devis" else "Au panier",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }
        }
    }
}
