package com.aistudio.perfumatico.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.aistudio.perfumatico.data.local.PerfumeEntity
import com.aistudio.perfumatico.ui.theme.*
import com.aistudio.perfumatico.ui.viewmodel.MainTab

@Composable
fun PerfumaticoTopBar(
    userName: String,
    isCloudConnected: Boolean,
    isAdmin: Boolean,
    onProfileClick: () -> Unit,
    onAddClick: () -> Unit,
    showAddButton: Boolean = true
) {
    Surface(
        color = Slate950,
        modifier = Modifier.fillMaxWidth(),
        border = null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Amber500, Amber400)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.LocalBar,
                        contentDescription = "Logo",
                        tint = Slate950,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "PERFUMÁTICO",
                            color = Amber400,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                        if (isAdmin) {
                            Surface(
                                color = Amber500.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(4.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Amber400)
                            ) {
                                Text(
                                    text = "ADMIN",
                                    color = Amber400,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (isCloudConnected) Emerald400 else Slate600)
                        )
                        Text(
                            text = userName.ifBlank { "COLECIONADOR" }.uppercase(),
                            color = Slate400,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (showAddButton) {
                    FilledTonalButton(
                        onClick = onAddClick,
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = Amber400,
                            contentColor = Slate950
                        ),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        modifier = Modifier.testTag("add_perfume_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Adicionar",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "ADICIONAR",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                IconButton(
                    onClick = onProfileClick,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Slate900)
                        .border(1.dp, Slate800, RoundedCornerShape(10.dp))
                        .testTag("profile_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Perfil",
                        tint = Amber400,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun PerfumaticoBottomNav(
    currentTab: MainTab,
    onTabSelected: (MainTab) -> Unit
) {
    NavigationBar(
        containerColor = Slate950,
        tonalElevation = 8.dp,
        modifier = Modifier.border(width = 1.dp, color = Slate900)
    ) {
        val items = listOf(
            Triple(MainTab.COLLECTION, "Meus", Icons.Default.Diamond),
            Triple(MainTab.DISCOVER, "Explorar", Icons.Default.Explore),
            Triple(MainTab.ORACLE, "Oráculo IA", Icons.Default.AutoAwesome),
            Triple(MainTab.DASHBOARD, "Painel", Icons.Default.Analytics)
        )

        items.forEach { (tab, label, icon) ->
            val selected = when (tab) {
                MainTab.DISCOVER -> currentTab == MainTab.DISCOVER || currentTab == MainTab.CATALOG
                MainTab.ORACLE -> currentTab == MainTab.ORACLE || currentTab == MainTab.CHATBOT
                else -> currentTab == tab
            }
            NavigationBarItem(
                alwaysShowLabel = true,
                selected = selected,
                onClick = { onTabSelected(tab) },
                icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = {
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Slate950,
                    selectedTextColor = Amber400,
                    indicatorColor = Amber400,
                    unselectedIconColor = Slate500,
                    unselectedTextColor = Slate500
                ),
                modifier = Modifier.testTag("nav_tab_${label.lowercase().replace(" ", "_")}")
            )
        }
    }
}

@Composable
fun PerfumeCard(
    perfume: PerfumeEntity,
    onClick: () -> Unit,
    onSotdClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = Slate900),
        shape = RoundedCornerShape(18.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.verticalGradient(listOf(Slate800, Slate900))),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("perfume_card_${perfume.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Image or stylish monogram
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Slate800)
                        .border(1.dp, Slate700, RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (perfume.imageUrl.isNotBlank()) {
                        AsyncImage(
                            model = perfume.imageUrl,
                            contentDescription = perfume.name,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(4.dp)
                        )
                    } else {
                        Text(
                            text = perfume.name.take(3).uppercase(),
                            color = Amber400,
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp
                        )
                    }
                }

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = perfume.brand.uppercase(),
                        color = Amber500,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = perfume.name,
                            color = Slate100,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        
                        val isNew = perfume.markedNewAt > 0L && (System.currentTimeMillis() - perfume.markedNewAt) < 14L * 24 * 60 * 60 * 1000
                        if (isNew) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = Emerald500.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(4.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Emerald500)
                            ) {
                                Text(
                                    text = "ADICIONAR",
                                    color = Emerald400,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                        if (perfume.tags.contains("ASSINATURA", true)) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(Icons.Default.Star, contentDescription = "Assinatura", tint = Amber400, modifier = Modifier.size(14.dp))
                        } else if (perfume.userPreference == 1) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(Icons.Default.Favorite, contentDescription = "Amo", tint = Rose500, modifier = Modifier.size(14.dp))
                        } else if (perfume.userPreference == 2) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(Icons.Default.ThumbUp, contentDescription = "Gosto", tint = Amber400, modifier = Modifier.size(14.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    val volumeText = try {
                        val arr = org.json.JSONArray(perfume.bottlesJson)
                        val first = arr.get(0)
                        if (first is org.json.JSONObject) first.getString("size") else first.toString()
                    } catch (e: Exception) {
                        if (perfume.bottlesJson.contains("50ml")) "50ml" else "100ml"
                    }

                    val typeText = if (volumeText.contains("Decant", ignoreCase = true) || volumeText.contains("Amostra", ignoreCase = true)) "Decant" else "Frasco"
                    val displayVol = volumeText.replace("Decant ", "", ignoreCase = true)
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            color = Slate800,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "$typeText • $displayVol",
                                color = Slate200,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        
                        Surface(
                            color = Slate800,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = perfume.family,
                                color = Slate300,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        if (perfume.referenceName.isNotBlank()) {
                            Surface(
                                color = Slate800,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "Insp: ${perfume.referenceName}",
                                    color = Emerald400,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Longevity & Projection bars
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatPill(
                        label = "Fixação",
                        value = "${perfume.fixation}/10",
                        color = if (perfume.fixation >= 8) Emerald400 else Amber400
                    )
                    StatPill(
                        label = "Projeção",
                        value = "${perfume.projection}/10",
                        color = if (perfume.projection >= 8) Cyan500 else Amber500
                    )
                }

                if (onSotdClick != null) {
                    IconButton(
                        onClick = onSotdClick,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Slate800)
                    ) {
                        Icon(
                            imageVector = Icons.Default.WbSunny,
                            contentDescription = "Scent of the Day",
                            tint = Amber400,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Tags display if available
            if (perfume.tags.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                val tagList = perfume.tags.split(",").map { it.trim() }.filter { it.isNotBlank() }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    tagList.take(3).forEach { tag ->
                        Surface(
                            color = Slate950,
                            shape = RoundedCornerShape(4.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Slate800)
                        ) {
                            Text(
                                text = tag,
                                color = Slate400,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatPill(
    label: String,
    value: String,
    color: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "$label:",
            color = Slate500,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.Black
        )
    }
}
