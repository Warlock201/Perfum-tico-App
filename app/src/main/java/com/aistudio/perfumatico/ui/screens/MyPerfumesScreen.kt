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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aistudio.perfumatico.data.local.PerfumeEntity
import com.aistudio.perfumatico.data.model.AVAILABLE_FAMILIES
import com.aistudio.perfumatico.data.model.AVAILABLE_TAGS
import com.aistudio.perfumatico.ui.components.PerfumeCard
import com.aistudio.perfumatico.ui.theme.*
import com.aistudio.perfumatico.ui.viewmodel.CollectionSubTab
import com.aistudio.perfumatico.ui.viewmodel.MainTab
import com.aistudio.perfumatico.ui.viewmodel.PerfumeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyPerfumesScreen(
    viewModel: PerfumeViewModel,
    modifier: Modifier = Modifier
) {
    val perfumes by viewModel.myPerfumes.collectAsState()
    val currentSubTab by viewModel.collectionSubTab.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedTag by viewModel.selectedTagFilter.collectAsState()
    val selectedFamily by viewModel.selectedFamilyFilter.collectAsState()

    var showFilters by remember { mutableStateOf(false) }

    // Filter perfumes by subTab, search, tag, family
    val filteredList = remember(perfumes, currentSubTab, searchQuery, selectedTag, selectedFamily) {
        perfumes.filter { p ->
            val matchesTab = p.status == currentSubTab.dbStatus || 
                (currentSubTab == CollectionSubTab.HAVE && (p.status == "Já possuo" || p.status == "Frasco" || p.status == "Decant")) ||
                (currentSubTab == CollectionSubTab.WANT && (p.status == "Quero ter" || p.status == "Pipeline")) ||
                (currentSubTab == CollectionSubTab.DREAM && (p.status == "Desejo" || p.status == "Desejos" || p.status == "Wishlist"))

            val matchesSearch = searchQuery.isBlank() ||
                    p.name.contains(searchQuery, ignoreCase = true) ||
                    p.brand.contains(searchQuery, ignoreCase = true) ||
                    p.notes.contains(searchQuery, ignoreCase = true)

            val matchesTag = selectedTag == null || p.tags.contains(selectedTag!!, ignoreCase = true)
            val matchesFamily = selectedFamily == null || p.family.equals(selectedFamily, ignoreCase = true)

            matchesTab && matchesSearch && matchesTag && matchesFamily
        }.sortedWith(
            compareByDescending<PerfumeEntity> { it.tags.contains("ASSINATURA", true) }
                .thenBy { if (it.userPreference in 1..4) it.userPreference else 99 }
                .thenBy { it.name }
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Slate950)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
        // Subtabs selector: FRASCOS, DECANTS, WISHLIST
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
            val tabs = listOf(
                CollectionSubTab.HAVE to "COLEÇÃO",
                CollectionSubTab.WANT to "QUERO TER",
                CollectionSubTab.DREAM to "DESEJO"
            )

            tabs.forEach { (tab, label) ->
                val selected = currentSubTab == tab
                Button(
                    onClick = { viewModel.setCollectionSubTab(tab) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selected) Slate700 else Color.Transparent,
                        contentColor = if (selected) Amber400 else Slate400
                    ),
                    shape = RoundedCornerShape(8.dp),
                    elevation = null,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("subtab_${label.lowercase().replace(" ", "_")}"),
                    contentPadding = PaddingValues(vertical = 10.dp)
                ) {
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        fontWeight = if (selected) FontWeight.Black else FontWeight.Bold
                    )
                }
            }
        }

        // Search & Filter bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = {
                    Text("Buscar perfume, marca...", color = Slate500, fontSize = 13.sp)
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
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Limpar",
                                tint = Slate400,
                                modifier = Modifier.size(18.dp)
                            )
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
                    .weight(1f)
                    .testTag("search_input")
            )

            IconButton(
                onClick = { showFilters = !showFilters },
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (showFilters || selectedTag != null || selectedFamily != null) Amber400 else Slate900)
                    .border(1.dp, Slate800, RoundedCornerShape(12.dp))
                    .testTag("filter_toggle_button")
            ) {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = "Filtros",
                    tint = if (showFilters || selectedTag != null || selectedFamily != null) Slate950 else Slate300
                )
            }
        }

        // Expandable Filters
        if (showFilters) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Slate900)
                    .border(1.dp, Slate800, RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = "OCASIÃO / TAG",
                    color = Amber400,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    AVAILABLE_TAGS.forEach { tag ->
                        val isSelected = selectedTag == tag
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.toggleTagFilter(tag) },
                            label = { Text(tag, fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Amber400,
                                selectedLabelColor = Slate950,
                                containerColor = Slate800,
                                labelColor = Slate300
                            ),
                            border = null
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "FAMÍLIA OLFATIVA",
                    color = Emerald400,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
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
                                viewModel.setFamilyFilter(if (isSelected) null else fam)
                            },
                            label = { Text(fam, fontSize = 10.sp) },
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

                if (selectedTag != null || selectedFamily != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(
                        onClick = {
                            viewModel.toggleTagFilter(selectedTag ?: "")
                            viewModel.setFamilyFilter(null)
                        },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Limpar Filtros", color = Amber400, fontSize = 12.sp)
                    }
                }
            }
        }

        // Counter info
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${filteredList.size} FRAGRÂNCIAS",
                color = Slate400,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }

        // List
        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalDrink,
                        contentDescription = null,
                        tint = Slate600,
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = "Nenhum perfume encontrado",
                        color = Slate300,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "Sua coleção está vazia nesta categoria.\nToque no botão flutuante + abaixo para adicionar!",
                        color = Slate500,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { viewModel.navigateToCatalog() },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Amber400
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Slate800),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Explorar Catálogo", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(filteredList, key = { it.id }) { perfume ->
                    PerfumeCard(
                        perfume = perfume,
                        onClick = { viewModel.openPerfumeDetails(perfume) },
                        onSotdClick = { viewModel.registerSotd(perfume, "Fragrância do dia!") }
                    )
                }
            }
        }
        }

        ExtendedFloatingActionButton(
            onClick = { viewModel.openAddChooser() },
            containerColor = Amber400,
            contentColor = Slate950,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .testTag("fab_add_perfume")
        ) {
            Icon(Icons.Default.Add, contentDescription = "Adicionar Perfume", modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("ADICIONAR", fontWeight = FontWeight.Black, fontSize = 12.sp)
        }
    }
}
