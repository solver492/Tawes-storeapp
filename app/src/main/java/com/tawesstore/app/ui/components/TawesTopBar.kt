package com.tawesstore.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tawesstore.app.data.model.ShoppingMode
import com.tawesstore.app.ui.theme.*

@Composable
fun TawesTopBar(
    currentMode: ShoppingMode,
    onModeChange: (ShoppingMode) -> Unit,
    cartItemCount: Int,
    onCartClick: () -> Unit
) {
    Surface(
        color = TawesSurfaceWhite,
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Announcement banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(TawesGreen)
                    .padding(vertical = 6.dp, horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "✦ Tawes Store · Vente au détail et en gros au Maroc",
                    color = TawesCream,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Main Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Logo & Brand Name
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.testTag("brand_header")
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp, bottomEnd = 10.dp, bottomStart = 3.dp))
                            .background(TawesGreen),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "T",
                            color = TawesSurfaceWhite,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            fontStyle = FontStyle.Italic,
                            fontFamily = FontFamily.Serif
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Row {
                        Text(
                            text = "Tawes",
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color = TawesInk
                        )
                        Text(
                            text = "Store",
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color = TawesOrange
                        )
                    }
                }

                // Actions: Cart
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Cart Button with badge
                    IconButton(
                        onClick = onCartClick,
                        modifier = Modifier
                            .size(38.dp)
                            .testTag("cart_button")
                    ) {
                        BadgedBox(
                            badge = {
                                if (cartItemCount > 0) {
                                    Badge(
                                        containerColor = TawesOrange,
                                        contentColor = TawesSurfaceWhite
                                    ) {
                                        Text("$cartItemCount", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (cartItemCount > 0) Icons.Filled.ShoppingCart else Icons.Outlined.ShoppingCart,
                                contentDescription = "Panier",
                                tint = if (cartItemCount > 0) TawesGreen else TawesInk
                            )
                        }
                    }
                }
            }

            // Mode Selector Pill (Détail vs Gros)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFEAF0EB))
                    .padding(3.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Detail Mode
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (currentMode == ShoppingMode.RETAIL) TawesSurfaceWhite else Color.Transparent)
                        .clickable { onModeChange(ShoppingMode.RETAIL) }
                        .padding(vertical = 8.dp)
                        .testTag("retail_mode_tab"),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Achat au détail",
                            fontSize = 12.sp,
                            fontWeight = if (currentMode == ShoppingMode.RETAIL) FontWeight.Bold else FontWeight.Medium,
                            color = if (currentMode == ShoppingMode.RETAIL) TawesGreen else TawesMuted
                        )
                        Text(
                            text = "Pour particuliers",
                            fontSize = 9.sp,
                            color = TawesMuted
                        )
                    }
                }

                // Wholesale Mode
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (currentMode == ShoppingMode.WHOLESALE) TawesSurfaceWhite else Color.Transparent)
                        .clickable { onModeChange(ShoppingMode.WHOLESALE) }
                        .padding(vertical = 8.dp)
                        .testTag("wholesale_mode_tab"),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Achat en gros",
                            fontSize = 12.sp,
                            fontWeight = if (currentMode == ShoppingMode.WHOLESALE) FontWeight.Bold else FontWeight.Medium,
                            color = if (currentMode == ShoppingMode.WHOLESALE) TawesWholesaleText else TawesMuted
                        )
                        Text(
                            text = "Pour revendeurs",
                            fontSize = 9.sp,
                            color = TawesWholesaleText
                        )
                    }
                }
            }
        }
    }
}
