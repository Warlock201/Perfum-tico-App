package com.aistudio.perfumatico.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aistudio.perfumatico.data.local.PerfumeEntity
import com.aistudio.perfumatico.data.model.AVAILABLE_FAMILIES
import com.aistudio.perfumatico.ui.components.PerfumeCard
import com.aistudio.perfumatico.ui.theme.*
import com.aistudio.perfumatico.ui.viewmodel.PerfumeViewModel
import com.aistudio.perfumatico.utils.matchesSearch

@Composable
fun DiscoverScreen(
    viewModel: PerfumeViewModel,
    modifier: Modifier = Modifier
) {
    val myPerfumes by viewModel.myPerfumes.collectAsState()
    val globalPerfumes by viewModel.globalPerfumes.collectAsState()
    val olfactoryNotes by viewModel.olfactoryNotes.collectAsState()

    val userAnalysis = remember(myPerfumes) {
        viewModel.getUserProfileAnalysis(myPerfumes)
    }

    val selectedFamily by viewModel.discoverFamily.collectAsState()
    val selectedNotes by viewModel.discoverNotes.collectAsState()

    var manualSearchQuery by remember { mutableStateOf("") }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }
    var refreshKey by remember { mutableStateOf(0) }

    // Existing perfume IDs to exclude from recommendations
    val myIds = remember(myPerfumes) { myPerfumes.map { it.id }.toSet() }

    // Recommendations by Family
    val recommendationsByFamily = remember(userAnalysis, globalPerfumes, myIds, refreshKey) {
        if (userAnalysis == null) emptyList()
        else {
            globalPerfumes
                .filter { it.family.equals(userAnalysis.topFamily, ignoreCase = true) && !myIds.contains(it.id) }
                .take(4)
        }
    }

    // Recommendations by Notes
    val recommendationsByNotes = remember(userAnalysis, globalPerfumes, myIds, refreshKey) {
        if (userAnalysis == null || userAnalysis.topNotes.isEmpty()) emptyList()
        else {
            globalPerfumes
                .filter { p ->
                    !myIds.contains(p.id) && userAnalysis.topNotes.any { tn ->
                        p.notes.contains(tn, ignoreCase = true)
                    }
                }
                .take(4)
        }
    }

    // Manual Discovery Matches
    val manualMatches = remember(selectedFamily, selectedNotes, manualSearchQuery, globalPerfumes, myIds, refreshKey) {
        if (selectedFamily == null && selectedNotes.isEmpty() && manualSearchQuery.isBlank()) {
            emptyList()
        } else {
            globalPerfumes.filter { p ->
                val matchesFamily = selectedFamily == null || p.family.equals(selectedFamily, ignoreCase = true)
                val matchesNotes = selectedNotes.isEmpty() || selectedNotes.any { note ->
                    p.notes.contains(note, ignoreCase = true)
                }
                val matchesSearch = p.matchesSearch(manualSearchQuery)

                matchesFamily && matchesNotes && matchesSearch
            }.take(10)
        }
    }

    Scaffold(
        snackbarHost = {
            snackbarMessage?.let { msg ->
                Snackbar(
                    containerColor = Slate900,
                    contentColor = Amber400,
                    modifier = Modifier.padding(16.dp),
                    action = {
                        TextButton(onClick = { snackbarMessage = null }) {
                            Text("OK", color = Amber400)
                        }
                    }
                ) {
                    Text(msg)
                }
            }
        },
        containerColor = Slate950
    ) { padding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .background(Slate950)
                .padding(padding)
                .padding(horizontal = 16.dp)
                .testTag("discover_screen"),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "DESCUBRA NOVAS FRAGRÂNCIAS",
                        color = Slate400,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    FilledTonalButton(
                        onClick = {
                            refreshKey++
                            snackbarMessage = "Recomendações atualizadas com sucesso!"
                        },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = Slate900,
                            contentColor = Amber400
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Slate800)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Atualizar Recomendações",
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "ATUALIZAR",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // AI/Algorithm Smart Recommendations Section
            if (userAnalysis != null) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Slate900),
                        shape = RoundedCornerShape(16.dp),
                        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.verticalGradient(listOf(Slate800, Slate900))),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = Amber400,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "SEU PERFIL OLFATIVO",
                                    color = Amber400,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Família preferida: ${userAnalysis.topFamily.uppercase()}",
                                color = Slate200,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            if (userAnalysis.topNotes.isNotEmpty()) {
                                Text(
                                    text = "Notas frequentes: ${userAnalysis.topNotes.joinToString(", ")}",
                                    color = Slate400,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }

                if (recommendationsByFamily.isNotEmpty()) {
                    item {
                        Text(
                            text = "PORQUE VOCÊ GOSTA DE ${userAnalysis.topFamily.uppercase()}",
                            color = Amber400,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }

                    items(recommendationsByFamily, key = { "rec_fam_${it.id}" }) { perfume ->
                        DiscoverItemCard(
                            perfume = perfume,
                            onAddWish = {
                                viewModel.quickAddFromCatalog(perfume, "Desejos")
                                snackbarMessage = "Adicionado aos Desejos!"
                            },
                            onAddHave = {
                                viewModel.quickAddFromCatalog(perfume, "Já possuo")
                                snackbarMessage = "Adicionado à Coleção!"
                            },
                            onClick = { viewModel.openPerfumeDetails(perfume) }
                        )
                    }
                }

                if (recommendationsByNotes.isNotEmpty()) {
                    item {
                        Text(
                            text = "COM SUAS NOTAS FAVORITAS",
                            color = Emerald400,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }

                    items(recommendationsByNotes, key = { "rec_not_${it.id}" }) { perfume ->
                        DiscoverItemCard(
                            perfume = perfume,
                            onAddWish = {
                                viewModel.quickAddFromCatalog(perfume, "Desejos")
                                snackbarMessage = "Adicionado aos Desejos!"
                            },
                            onAddHave = {
                                viewModel.quickAddFromCatalog(perfume, "Já possuo")
                                snackbarMessage = "Adicionado à Coleção!"
                            },
                            onClick = { viewModel.openPerfumeDetails(perfume) }
                        )
                    }
                }
            }

            // Manual Explorer Section
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Slate900),
                    shape = RoundedCornerShape(16.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.verticalGradient(listOf(Slate800, Slate900))),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Explore,
                                contentDescription = null,
                                tint = Cyan500,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "EXPLORADOR MANUAL DE NOTAS",
                                color = Cyan500,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                        // Family selection
                        Text(
                            text = "ESCOLHA UMA FAMÍLIA:",
                            color = Slate400,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            AVAILABLE_FAMILIES.forEach { fam ->
                                val isSelected = selectedFamily == fam
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        viewModel.setDiscoverFamily(if (isSelected) null else fam)
                                    },
                                    label = { Text(fam, fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Cyan500,
                                        selectedLabelColor = Slate950,
                                        containerColor = Slate800,
                                        labelColor = Slate300
                                    ),
                                    border = null
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Olfactory Notes selection
                        Text(
                            text = "SELECIONE NOTAS CHAVE:",
                            color = Slate400,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val popularNotes = listOf("BERGAMOTA", "MAÇÃ", "BAUNILHA", "ÂMBAR", "PATCHOULI", "LAVANDA", "CEDRO", "MUSK", "PIMENTA", "ÍRIS", "SÂNDALO")
                            popularNotes.forEach { note ->
                                val isSelected = selectedNotes.contains(note)
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { viewModel.toggleDiscoverNote(note) },
                                    label = { Text(note, fontSize = 10.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Emerald400,
                                        selectedLabelColor = Slate950,
                                        containerColor = Slate800,
                                        labelColor = Slate300
                                    ),
                                    border = null
                                )
                            }
                        }

                        if (selectedFamily != null || selectedNotes.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            TextButton(
                                onClick = { viewModel.clearDiscoverFilters() },
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Text("Limpar Seleção", color = Amber400, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            if (manualMatches.isNotEmpty()) {
                item {
                    Text(
                        text = "RESULTADOS ENCONTRADOS (${manualMatches.size})",
                        color = Cyan500,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }

                items(manualMatches, key = { "manual_${it.id}" }) { perfume ->
                    DiscoverItemCard(
                        perfume = perfume,
                        onAddWish = {
                            viewModel.quickAddFromCatalog(perfume, "Desejos")
                            snackbarMessage = "Adicionado aos Desejos!"
                        },
                        onAddHave = {
                            viewModel.quickAddFromCatalog(perfume, "Já possuo")
                            snackbarMessage = "Adicionado à Coleção!"
                        },
                        onClick = { viewModel.openPerfumeDetails(perfume) }
                    )
                }
            }
        }
    }
}

@Composable
fun DiscoverItemCard(
    perfume: PerfumeEntity,
    onAddWish: () -> Unit,
    onAddHave: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = Slate900),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.verticalGradient(listOf(Slate800, Slate900))),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = perfume.brand.uppercase(),
                        color = Amber500,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = perfume.name,
                        color = Slate100,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${perfume.family} • ${perfume.notes.take(40)}...",
                        color = Slate400,
                        fontSize = 11.sp,
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onAddWish,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Cyan500),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Cyan500.copy(alpha = 0.5f)),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(imageVector = Icons.Default.BookmarkAdd, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("+ Quero", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.width(8.dp))

                FilledTonalButton(
                    onClick = onAddHave,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(containerColor = Amber400, contentColor = Slate950),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("+ Tenho", fontSize = 11.sp, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}
