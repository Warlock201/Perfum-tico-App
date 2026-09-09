package com.aistudio.perfumatico.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.aistudio.perfumatico.data.local.PerfumeEntity
import com.aistudio.perfumatico.data.model.AVAILABLE_TAGS
import com.aistudio.perfumatico.data.model.AVAILABLE_VOLUMES
import com.aistudio.perfumatico.ui.theme.*
import com.aistudio.perfumatico.ui.viewmodel.PerfumeViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PerfumeDetailDialog(
    perfume: PerfumeEntity,
    viewModel: PerfumeViewModel,
    onDismiss: () -> Unit
) {
    var fixation by remember { mutableFloatStateOf(perfume.fixation.toFloat()) }
    var projection by remember { mutableFloatStateOf(perfume.projection.toFloat()) }
    var personalNotes by remember { mutableStateOf(perfume.personalNotes) }
    var status by remember { mutableStateOf(perfume.status) }

    val currentTags = remember(perfume.tags) {
        perfume.tags.split(",").map { it.trim() }.filter { it.isNotBlank() }.toMutableStateList()
    }

    var selectedVolume by remember {
        mutableStateOf(if (perfume.bottlesJson.contains("50ml")) "50ml" else "100ml")
    }

    var showDeleteConfirm by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f)
                .clip(RoundedCornerShape(24.dp))
                .testTag("perfume_detail_dialog"),
            color = Slate900,
            border = androidx.compose.foundation.BorderStroke(1.dp, Slate800)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header: Close and Delete / Edit
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Slate800)
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Fechar", tint = Slate300, modifier = Modifier.size(18.dp))
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(
                            onClick = {
                                onDismiss()
                                viewModel.openAddEdit(perfume)
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Slate800)
                        ) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = "Editar", tint = Amber400, modifier = Modifier.size(18.dp))
                        }

                        IconButton(
                            onClick = { showDeleteConfirm = true },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Slate800)
                        ) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Excluir", tint = Rose500, modifier = Modifier.size(18.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Scrollable Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Title & Brand
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Slate800)
                                .border(1.dp, Slate700, RoundedCornerShape(16.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (perfume.imageUrl.isNotBlank()) {
                                AsyncImage(
                                    model = perfume.imageUrl,
                                    contentDescription = perfume.name,
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier.fillMaxSize().padding(4.dp)
                                )
                            } else {
                                Text(
                                    text = perfume.name.take(3).uppercase(),
                                    color = Amber400,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 20.sp
                                )
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = perfume.brand.uppercase(),
                                color = Amber500,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = perfume.name,
                                color = Slate50,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Surface(
                                    color = Slate800,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = perfume.family,
                                        color = Slate300,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                if (perfume.referenceName.isNotBlank()) {
                                    Surface(
                                        color = Slate800,
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = "Dupe: ${perfume.referenceName}",
                                            color = Emerald400,
                                            fontSize = 11.sp,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Olfactory Pyramid
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Slate950),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Slate800),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("PIRÂMIDE OLFATIVA", color = Amber400, fontSize = 11.sp, fontWeight = FontWeight.Black)

                            if (perfume.topNotes.isNotBlank()) {
                                PyramidTier(tier = "Saída (Top)", notes = perfume.topNotes, color = Cyan500)
                            }
                            if (perfume.heartNotes.isNotBlank()) {
                                PyramidTier(tier = "Coração (Heart)", notes = perfume.heartNotes, color = Emerald400)
                            }
                            if (perfume.baseNotes.isNotBlank()) {
                                PyramidTier(tier = "Fundo (Base)", notes = perfume.baseNotes, color = Amber400)
                            }
                            if (perfume.topNotes.isBlank() && perfume.heartNotes.isBlank() && perfume.baseNotes.isBlank() && perfume.notes.isNotBlank()) {
                                Text(perfume.notes, color = Slate300, fontSize = 12.sp)
                            }
                        }
                    }

                    // Status Switcher: Já possuo / Pipeline / Desejos
                    Column {
                        Text("STATUS NA COLEÇÃO", color = Slate400, fontSize = 10.sp, fontWeight = FontWeight.Black)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("Já possuo", "Pipeline", "Desejos").forEach { st ->
                                val isSel = status == st
                                FilterChip(
                                    selected = isSel,
                                    onClick = { status = st },
                                    label = { Text(st, fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = if (st == "Já possuo") Amber400 else if (st == "Pipeline") Emerald400 else Cyan500,
                                        selectedLabelColor = Slate950,
                                        containerColor = Slate800,
                                        labelColor = Slate300
                                    ),
                                    border = null,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    // Fixation & Projection Sliders
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Slate950),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Slate800),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("AVALIAÇÃO PESSOAL (PERFORMANCE)", color = Amber400, fontSize = 11.sp, fontWeight = FontWeight.Black)

                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Fixação na pele", color = Slate300, fontSize = 12.sp)
                                    Text("${fixation.toInt()}/10", color = Emerald400, fontSize = 12.sp, fontWeight = FontWeight.Black)
                                }
                                Slider(
                                    value = fixation,
                                    onValueChange = { fixation = it },
                                    valueRange = 1f..10f,
                                    steps = 8,
                                    colors = SliderDefaults.colors(
                                        thumbColor = Emerald400,
                                        activeTrackColor = Emerald400,
                                        inactiveTrackColor = Slate800
                                    )
                                )
                            }

                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Projeção / Rastro", color = Slate300, fontSize = 12.sp)
                                    Text("${projection.toInt()}/10", color = Cyan500, fontSize = 12.sp, fontWeight = FontWeight.Black)
                                }
                                Slider(
                                    value = projection,
                                    onValueChange = { projection = it },
                                    valueRange = 1f..10f,
                                    steps = 8,
                                    colors = SliderDefaults.colors(
                                        thumbColor = Cyan500,
                                        activeTrackColor = Cyan500,
                                        inactiveTrackColor = Slate800
                                    )
                                )
                            }
                        }
                    }

                    // Volume selector
                    Column {
                        Text("FRASCO / VOLUME", color = Slate400, fontSize = 10.sp, fontWeight = FontWeight.Black)
                        Spacer(modifier = Modifier.height(6.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("100ml", "50ml", "200ml", "Decant 10ml", "Decant 5ml", "Amostra").forEach { vol ->
                                val isSel = selectedVolume == vol
                                FilterChip(
                                    selected = isSel,
                                    onClick = { selectedVolume = vol },
                                    label = { Text(vol, fontSize = 11.sp) },
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
                    }

                    // Tags Selector
                    Column {
                        Text("OCASIÕES & TAGS", color = Slate400, fontSize = 10.sp, fontWeight = FontWeight.Black)
                        Spacer(modifier = Modifier.height(6.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            AVAILABLE_TAGS.forEach { tag ->
                                val isSel = currentTags.contains(tag)
                                FilterChip(
                                    selected = isSel,
                                    onClick = {
                                        if (isSel) currentTags.remove(tag) else currentTags.add(tag)
                                    },
                                    label = { Text(tag, fontSize = 10.sp) },
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
                    }

                    // Personal Notes Field
                    OutlinedTextField(
                        value = personalNotes,
                        onValueChange = { personalNotes = it },
                        label = { Text("Anotações Pessoais") },
                        placeholder = { Text("Ex: Bom para noites frias, rende muitos elogios...") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Amber400,
                            unfocusedBorderColor = Slate800,
                            focusedTextColor = Slate100,
                            unfocusedTextColor = Slate100,
                            focusedContainerColor = Slate950,
                            unfocusedContainerColor = Slate950
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Action buttons: Usar Hoje & Salvar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            viewModel.registerSotd(perfume, "Fragrância do dia!")
                            onDismiss()
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Amber400),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Amber400),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(imageVector = Icons.Default.WbSunny, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Usar Hoje", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    Button(
                        onClick = {
                            val updated = perfume.copy(
                                status = status,
                                fixation = fixation.toInt(),
                                projection = projection.toInt(),
                                tags = currentTags.joinToString(", "),
                                personalNotes = personalNotes.trim(),
                                bottlesJson = "[\"$selectedVolume\"]"
                            )
                            viewModel.savePerfume(updated)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Amber400, contentColor = Slate950),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("save_perfume_details_button")
                    ) {
                        Text("Salvar", fontWeight = FontWeight.Black, fontSize = 13.sp)
                    }
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Remover Perfume") },
            text = { Text("Deseja realmente remover '${perfume.name}' da sua coleção?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        viewModel.deletePerfume(perfume)
                    }
                ) {
                    Text("Excluir", color = Rose500)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancelar", color = Slate400)
                }
            },
            containerColor = Slate900,
            titleContentColor = Slate100,
            textContentColor = Slate300
        )
    }
}

@Composable
fun PyramidTier(
    tier: String,
    notes: String,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = "$tier:",
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(90.dp)
        )
        Text(
            text = notes,
            color = Slate200,
            fontSize = 11.sp,
            modifier = Modifier.weight(1f)
        )
    }
}
