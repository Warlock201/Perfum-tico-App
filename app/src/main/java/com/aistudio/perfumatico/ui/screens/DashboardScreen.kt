package com.aistudio.perfumatico.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aistudio.perfumatico.ui.theme.*
import com.aistudio.perfumatico.ui.viewmodel.PerfumeViewModel
import java.text.NumberFormat
import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: PerfumeViewModel,
    modifier: Modifier = Modifier
) {
    val perfumes by viewModel.myPerfumes.collectAsState()

    val currencyFormat = NumberFormat.getCurrencyInstance(java.util.Locale.forLanguageTag("pt-BR"))

    val haveList = perfumes.filter { it.status == "Já possuo" || it.status == "Frasco" || it.status == "Decant" }
    val toBuyList = perfumes.filter { it.status == "Quero ter" || it.status == "Pipeline" }
    val wishList = perfumes.filter { it.status == "Desejo" || it.status == "Desejos" || it.status == "Wishlist" }

    val patrimonioMin = haveList.sumOf { it.priceMin }
    val patrimonioMax = haveList.sumOf { it.priceMax }

    val metaMin = toBuyList.sumOf { it.priceMin }
    val metaMax = toBuyList.sumOf { it.priceMax }

    val avgFixation = if (haveList.isNotEmpty()) {
        haveList.map { it.fixation }.average()
    } else 0.0

    val avgProjection = if (haveList.isNotEmpty()) {
        haveList.map { it.projection }.average()
    } else 0.0

    // Top families
    val familyCounts = haveList.groupingBy { it.family.ifBlank { "Fresco" } }.eachCount()
    val topFamilies = familyCounts.entries.sortedByDescending { it.value }.take(5)

    // Top tags
    val tagCounts = mutableMapOf<String, Int>()
    haveList.forEach { p ->
        p.tags.split(",").map { it.trim() }.filter { it.isNotBlank() }.forEach { tag ->
            tagCounts[tag] = (tagCounts[tag] ?: 0) + 1
        }
    }
    val topTags = tagCounts.entries.sortedByDescending { it.value }.take(5)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Slate950)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("dashboard_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "VISÃO GERAL DO ACERVO",
            color = Slate400,
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp
        )

        // Financial & Collection summary cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DashboardMetricCard(
                title = "PATRIMÔNIO ATUAL",
                subtitle = "${currencyFormat.format(patrimonioMin)} - ${currencyFormat.format(patrimonioMax)}",
                icon = Icons.Default.AccountBalanceWallet,
                color = Amber400,
                modifier = Modifier.weight(1f)
            )

            DashboardMetricCard(
                title = "META DE COMPRAS",
                subtitle = "${currencyFormat.format(metaMin)} - ${currencyFormat.format(metaMax)}",
                icon = Icons.Default.TrendingUp,
                color = Emerald400,
                modifier = Modifier.weight(1f)
            )
        }

        // Status counts
        Card(
            colors = CardDefaults.cardColors(containerColor = Slate900),
            shape = RoundedCornerShape(16.dp),
            border = CardDefaults.outlinedCardBorder().copy(brush = Brush.verticalGradient(listOf(Slate800, Slate900))),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "DISTRIBUIÇÃO DA COLEÇÃO",
                    color = Amber400,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    StatusCountItem(count = haveList.size, label = "TENHO", color = Amber400)
                    StatusCountItem(count = toBuyList.size, label = "PIPELINE", color = Emerald400)
                    StatusCountItem(count = wishList.size, label = "DESEJOS", color = Cyan500)
                }
            }
        }

        // Average Performance (Fixation & Projection)
        Card(
            colors = CardDefaults.cardColors(containerColor = Slate900),
            shape = RoundedCornerShape(16.dp),
            border = CardDefaults.outlinedCardBorder().copy(brush = Brush.verticalGradient(listOf(Slate800, Slate900))),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "DESEMPENHO MÉDIO DA COLEÇÃO",
                    color = Amber400,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Fixação", color = Slate300, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Text(String.format(Locale.US, "%.1f / 10", avgFixation), color = Emerald400, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { (avgFixation / 10f).toFloat().coerceIn(0f, 1f) },
                            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                            color = Emerald400,
                            trackColor = Slate800
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Projeção", color = Slate300, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Text(String.format(Locale.US, "%.1f / 10", avgProjection), color = Cyan500, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { (avgProjection / 10f).toFloat().coerceIn(0f, 1f) },
                            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                            color = Cyan500,
                            trackColor = Slate800
                        )
                    }
                }
            }
        }

        // Top Families
        Card(
            colors = CardDefaults.cardColors(containerColor = Slate900),
            shape = RoundedCornerShape(16.dp),
            border = CardDefaults.outlinedCardBorder().copy(brush = Brush.verticalGradient(listOf(Slate800, Slate900))),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "TOP FAMÍLIAS OLFATIVAS",
                    color = Amber400,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                if (topFamilies.isEmpty()) {
                    Text("Nenhum perfume na coleção ainda.", color = Slate500, fontSize = 12.sp)
                } else {
                    topFamilies.forEach { (family, count) ->
                        val ratio = if (haveList.isNotEmpty()) count.toFloat() / haveList.size else 0f
                        Column(modifier = Modifier.padding(vertical = 4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(family, color = Slate200, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                Text("$count perfumes", color = Amber400, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { ratio },
                                modifier = Modifier.fillMaxWidth().height(5.dp).clip(RoundedCornerShape(3.dp)),
                                color = Amber500,
                                trackColor = Slate800
                            )
                        }
                    }
                }
            }
        }

        // Top Tags
        Card(
            colors = CardDefaults.cardColors(containerColor = Slate900),
            shape = RoundedCornerShape(16.dp),
            border = CardDefaults.outlinedCardBorder().copy(brush = Brush.verticalGradient(listOf(Slate800, Slate900))),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "OCASIÕES & TAGS MAIS USADAS",
                    color = Emerald400,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                if (topTags.isEmpty()) {
                    Text("Nenhuma tag atribuída ainda.", color = Slate500, fontSize = 12.sp)
                } else {
                    topTags.forEach { (tag, count) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = Slate800,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = tag,
                                    color = Slate200,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                            Text(
                                text = "$count frascos",
                                color = Emerald400,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun DashboardMetricCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Slate900),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.verticalGradient(listOf(Slate800, Slate900))),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Slate800),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = title,
                color = Slate400,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                color = color,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun StatusCountItem(
    count: Int,
    label: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "$count",
            color = color,
            fontSize = 24.sp,
            fontWeight = FontWeight.Black
        )
        Text(
            text = label,
            color = Slate400,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
    }
}
