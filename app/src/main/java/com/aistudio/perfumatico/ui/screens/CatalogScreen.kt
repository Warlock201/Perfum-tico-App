package com.aistudio.perfumatico.ui.screens
import androidx.compose.ui.platform.LocalContext
import com.aistudio.perfumatico.utils.getNoteDrawableResId
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.Image
import androidx.compose.ui.text.style.TextAlign

import com.aistudio.perfumatico.utils.normalizeNoteName
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
import androidx.compose.ui.platform.LocalFocusManager
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
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import com.aistudio.perfumatico.utils.getNoteImageUrl

@Composable
fun CatalogScreen(
    viewModel: PerfumeViewModel,
    modifier: Modifier = Modifier
) {
    val currentSubTab by viewModel.catalogSubTab.collectAsState()
    val noteImages by viewModel.noteImages.collectAsState()
    
    val globalPerfumes by viewModel.globalPerfumes.collectAsState()
    val olfactoryNotes by viewModel.olfactoryNotes.collectAsState()
    val searchNotes by viewModel.searchNotes.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    val filteredPerfumes = remember(globalPerfumes, searchQuery, searchNotes) {
        var result = globalPerfumes
        if (searchNotes.isNotEmpty()) {
            result = result.filter { p ->
                val pNotes = p.notes.split("|").map { it.trim().lowercase() }
                searchNotes.all { sNote ->
                    pNotes.any { it.contains(sNote, ignoreCase = true) }
                }
            }
        }
        if (searchQuery.isNotBlank()) {
            result = result.filter { p ->
                p.name.contains(searchQuery, ignoreCase = true) ||
                        p.brand.contains(searchQuery, ignoreCase = true) ||
                        p.family.contains(searchQuery, ignoreCase = true) ||
                        p.notes.contains(searchQuery, ignoreCase = true)
            }
        }
        result
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
            // Subtabs: CATÁLOGO GERAL / RECOMENDAÇÕES / NOTAS OLFATIVAS
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
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    Text("CATÁLOGO", fontSize = 10.sp, fontWeight = FontWeight.Black)
                }

                Button(
                    onClick = { viewModel.setCatalogSubTab(CatalogSubTab.RECOMMENDATIONS) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (currentSubTab == CatalogSubTab.RECOMMENDATIONS) Amber400 else Color.Transparent,
                        contentColor = if (currentSubTab == CatalogSubTab.RECOMMENDATIONS) Slate950 else Slate400
                    ),
                    shape = RoundedCornerShape(8.dp),
                    elevation = null,
                    modifier = Modifier.weight(1.2f),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    Text("RECOMENDAÇÕES", fontSize = 10.sp, fontWeight = FontWeight.Black)
                }

                Button(
                    onClick = { viewModel.setCatalogSubTab(CatalogSubTab.NOTES) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (currentSubTab == CatalogSubTab.NOTES) Emerald400 else Color.Transparent,
                        contentColor = if (currentSubTab == CatalogSubTab.NOTES) Slate950 else Slate400
                    ),
                    shape = RoundedCornerShape(8.dp),
                    elevation = null,
                    modifier = Modifier.weight(0.9f),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    Text("NOTAS", fontSize = 10.sp, fontWeight = FontWeight.Black)
                }
            }

            if (currentSubTab == CatalogSubTab.RECOMMENDATIONS) {
                DiscoverScreen(
                    viewModel = viewModel,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
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

            // Search Notes Chips
            if (searchNotes.isNotEmpty()) {
                androidx.compose.foundation.lazy.LazyRow(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(searchNotes) { note ->
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Amber400.copy(alpha = 0.1f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Amber400.copy(alpha = 0.5f)),
                            onClick = { viewModel.toggleSearchNote(note) }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(note, color = Amber400, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.Default.Close, contentDescription = "Remover", tint = Amber400, modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                }
            }

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
                                        .background(Slate700),
                                    contentAlignment = Alignment.Center
                                ) {
                                    val context = LocalContext.current
                                    val resId = context.getNoteDrawableResId(note.nome)
                                    if (resId != 0) {
                                        Image(
                                            painter = painterResource(id = resId),
                                            contentDescription = note.nome,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        val dbUrl = noteImages[note.nome.normalizeNoteName()]
                                        val finalUrl = if (dbUrl.isNullOrBlank()) {
                                            com.aistudio.perfumatico.data.local.DefaultNoteImages[note.nome.normalizeNoteName()] 
                                            ?: note.nome.getNoteImageUrl()
                                        } else dbUrl

                                        if (finalUrl.isBlank()) {
                                            Icon(
                                                imageVector = Icons.Default.Spa,
                                                contentDescription = null,
                                                tint = Emerald400,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        } else {
                                            coil.compose.SubcomposeAsyncImage(
                                                model = finalUrl,
                                                contentDescription = null,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize(),
                                                loading = {
                                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                                        CircularProgressIndicator(color = Emerald400, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                                    }
                                                },
                                                error = {
                                                    Box(modifier = Modifier.fillMaxSize().background(Slate700), contentAlignment = Alignment.Center) {
                                                        Icon(imageVector = Icons.Default.Spa, contentDescription = null, tint = Slate400, modifier = Modifier.size(16.dp))
                                                    }
                                                }
                                            )
                                        }
                                    }
                                }
                                androidx.compose.material3.Text(
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
}

@Composable
fun CatalogPerfumeCard(
    perfume: com.aistudio.perfumatico.data.local.PerfumeEntity,
    onClick: () -> Unit,
    onAddWish: () -> Unit,
    onAddHave: () -> Unit
) {
    androidx.compose.material3.Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = Slate800)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Slate700)
            ) {
                coil.compose.AsyncImage(
                    model = perfume.imageUrl,
                    contentDescription = perfume.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                androidx.compose.material3.Text(
                    text = perfume.brand,
                    color = Slate300,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                androidx.compose.material3.Text(
                    text = perfume.name,
                    color = Slate50,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.FilterVintage,
                        contentDescription = null,
                        tint = Emerald400,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    androidx.compose.material3.Text(
                        text = perfume.family,
                        color = Emerald400,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row {
                    androidx.compose.material3.OutlinedButton(
                        onClick = onAddWish,
                        shape = RoundedCornerShape(8.dp),
                        colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(contentColor = Cyan500),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Cyan500.copy(alpha = 0.5f)),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(imageVector = Icons.Default.BookmarkBorder, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        androidx.compose.material3.Text("Quero", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    androidx.compose.material3.FilledTonalButton(
                        onClick = onAddHave,
                        shape = RoundedCornerShape(8.dp),
                        colors = androidx.compose.material3.ButtonDefaults.filledTonalButtonColors(containerColor = Amber400, contentColor = Slate950),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        androidx.compose.material3.Text("Tenho", fontSize = 11.sp, fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    }
}
