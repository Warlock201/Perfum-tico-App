package com.aistudio.perfumatico.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.aistudio.perfumatico.data.local.PerfumeEntity
import com.aistudio.perfumatico.ui.theme.*
import com.aistudio.perfumatico.ui.viewmodel.CatalogSubTab
import com.aistudio.perfumatico.ui.viewmodel.PerfumeViewModel

@Composable
fun CatalogScreen(
    viewModel: PerfumeViewModel,
    modifier: Modifier = Modifier
) {
    val currentSubTab by viewModel.catalogSubTab.collectAsState()
    val globalPerfumes = viewModel.globalPerfumes
    val olfactoryNotes = viewModel.olfactoryNotes

    var searchQuery by remember { mutableStateOf("") }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    val filteredPerfumes = remember(globalPerfumes, searchQuery) {
        if (searchQuery.isBlank()) globalPerfumes
        else {
            globalPerfumes.filter { p ->
                p.name.contains(searchQuery, ignoreCase = true) ||
                        p.brand.contains(searchQuery, ignoreCase = true) ||
                        p.family.contains(searchQuery, ignoreCase = true) ||
                        p.notes.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    val filteredNotes = remember(olfactoryNotes, searchQuery) {
        if (searchQuery.isBlank()) olfactoryNotes
        else {
            olfactoryNotes.filter { n ->
                n.nome.contains(searchQuery, ignoreCase = true)
            }
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
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(Slate950)
                .padding(padding)
                .padding(horizontal = 16.dp)
                .testTag("catalog_screen")
        ) {
            // Subtabs: CATÁLOGO GERAL / NOTAS OLFATIVAS
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Slate900)
                    .border(1.dp, Slate800, RoundedCornerShape(12.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Button(
                    onClick = { viewModel.setCatalogSubTab(CatalogSubTab.PERFUMES) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (currentSubTab == CatalogSubTab.PERFUMES) Amber400 else Color.Transparent,
                        contentColor = if (currentSubTab == CatalogSubTab.PERFUMES) Slate950 else Slate400
                    ),
                    shape = RoundedCornerShape(8.dp),
                    elevation = null,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(vertical = 10.dp)
                ) {
                    Text("CATÁLOGO GERAL", fontSize = 11.sp, fontWeight = FontWeight.Black)
                }

                Button(
                    onClick = { viewModel.setCatalogSubTab(CatalogSubTab.NOTES) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (currentSubTab == CatalogSubTab.NOTES) Emerald400 else Color.Transparent,
                        contentColor = if (currentSubTab == CatalogSubTab.NOTES) Slate950 else Slate400
                    ),
                    shape = RoundedCornerShape(8.dp),
                    elevation = null,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(vertical = 10.dp)
                ) {
                    Text("NOTAS OLFATIVAS", fontSize = 11.sp, fontWeight = FontWeight.Black)
                }
            }

            // Search input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text(
                        if (currentSubTab == CatalogSubTab.PERFUMES) "Buscar perfume, marca, nota..." else "Buscar nota (ex: Bergamota, Âmbar)...",
                        color = Slate500,
                        fontSize = 13.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Buscar",
                        tint = Slate500,
                        modifier = Modifier.size(20.dp)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Limpar", tint = Slate400, modifier = Modifier.size(18.dp))
                        }
                    }
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Amber400,
                    unfocusedBorderColor = Slate800,
                    focusedTextColor = Slate100,
                    unfocusedTextColor = Slate100,
                    focusedContainerColor = Slate900,
                    unfocusedContainerColor = Slate900
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
            )

            // Content
            if (currentSubTab == CatalogSubTab.PERFUMES) {
                Text(
                    text = "${filteredPerfumes.size} PERFUMES NO CATÁLOGO",
                    color = Slate400,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                    modifier = Modifier.padding(vertical = 6.dp)
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(filteredPerfumes, key = { it.id }) { perfume ->
                        CatalogPerfumeCard(
                            perfume = perfume,
                            onAddHave = {
                                viewModel.quickAddFromCatalog(perfume, "Já possuo")
                                snackbarMessage = "${perfume.name} adicionado à coleção!"
                            },
                            onAddWish = {
                                viewModel.quickAddFromCatalog(perfume, "Desejos")
                                snackbarMessage = "${perfume.name} adicionado aos desejos!"
                            },
                            onClick = { viewModel.openPerfumeDetails(perfume) }
                        )
                    }
                }
            } else {
                Text(
                    text = "${filteredNotes.size} NOTAS REGISTRADAS",
                    color = Slate400,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                    modifier = Modifier.padding(vertical = 6.dp)
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(filteredNotes, key = { it.id }) { note ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Slate900),
                            shape = RoundedCornerShape(12.dp),
                            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Slate800)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Emerald500.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Spa,
                                        contentDescription = null,
                                        tint = Emerald400,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Text(
                                    text = note.nome,
                                    color = Slate100,
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
}

@Composable
fun CatalogPerfumeCard(
    perfume: PerfumeEntity,
    onAddHave: () -> Unit,
    onAddWish: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = Slate900),
        shape = RoundedCornerShape(14.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Slate800)),
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
                        text = "${perfume.family} • R$ ${perfume.priceMin.toInt()} - ${perfume.priceMax.toInt()}",
                        color = Slate400,
                        fontSize = 11.sp
                    )
                    if (perfume.notes.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = perfume.notes,
                            color = Slate500,
                            fontSize = 10.sp,
                            maxLines = 1
                        )
                    }
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
                    Icon(imageVector = Icons.Default.BookmarkBorder, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Quero", fontSize = 11.sp, fontWeight = FontWeight.Bold)
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
                    Text("Tenho", fontSize = 11.sp, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}
