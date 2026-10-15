package com.tawesstore.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
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
fun HeroBanner(
    currentMode: ShoppingMode,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        if (currentMode == ShoppingMode.WHOLESALE) {
            // Wholesale Banner
            Card(
                colors = CardDefaults.cardColors(containerColor = TawesWholesaleBg),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("wholesale_info_banner")
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFE3BD98)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Store,
                            contentDescription = null,
                            tint = TawesWholesaleText
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Tarifs Grossiste & Revendeurs",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TawesWholesaleText
                        )
                        Text(
                            text = "Accédez aux prix de gros, minimums de commande avantageux et approvisionnements directs.",
                            fontSize = 11.sp,
                            color = Color(0xFF856F5B),
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        } else {
            // Retail Hero Banner
            Card(
                colors = CardDefaults.cardColors(containerColor = TawesSurfaceWhite),
                shape = RoundedCornerShape(14.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("retail_hero_banner")
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "LA NOUVELLE FAÇON D'ACHETER",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = TawesOrange,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Tout ce qu'il vous faut.",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TawesInk
                    )
                    Text(
                        text = "Au bon prix.",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        fontStyle = FontStyle.Italic,
                        fontFamily = FontFamily.Serif,
                        color = TawesGreen
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    // Trust elements row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TrustBadge(icon = Icons.Filled.CheckCircle, label = "Qualité choisie")
                        TrustBadge(icon = Icons.Filled.LocalShipping, label = "Livraison Maroc")
                        TrustBadge(icon = Icons.Filled.Payments, label = "Paiement livraison")
                    }
                }
            }
        }
    }
}

@Composable
private fun TrustBadge(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = TawesGreen,
            modifier = Modifier.size(13.dp)
        )
        Text(
            text = label,
            fontSize = 10.sp,
            color = TawesMuted,
            fontWeight = FontWeight.Medium
        )
    }
}
