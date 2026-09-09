package com.aistudio.perfumatico.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.aistudio.perfumatico.data.local.PerfumeEntity
import com.aistudio.perfumatico.data.model.AVAILABLE_FAMILIES
import com.aistudio.perfumatico.ui.theme.*
import com.aistudio.perfumatico.ui.viewmodel.PerfumeViewModel
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditPerfumeDialog(
    perfumeToEdit: PerfumeEntity?,
    viewModel: PerfumeViewModel,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(perfumeToEdit?.name ?: "") }
    var brand by remember { mutableStateOf(perfumeToEdit?.brand ?: "") }
    var family by remember { mutableStateOf(perfumeToEdit?.family ?: "Fresco") }
    var imageUrl by remember { mutableStateOf(perfumeToEdit?.imageUrl ?: "") }
    var priceMin by remember { mutableStateOf(perfumeToEdit?.priceMin?.toInt()?.toString() ?: "200") }
    var priceMax by remember { mutableStateOf(perfumeToEdit?.priceMax?.toInt()?.toString() ?: "350") }
    var topNotes by remember { mutableStateOf(perfumeToEdit?.topNotes ?: "") }
    var heartNotes by remember { mutableStateOf(perfumeToEdit?.heartNotes ?: "") }
    var baseNotes by remember { mutableStateOf(perfumeToEdit?.baseNotes ?: "") }
    var referenceName by remember { mutableStateOf(perfumeToEdit?.referenceName ?: "") }
    var status by remember { mutableStateOf(perfumeToEdit?.status ?: "Já possuo") }

    val isEditing = perfumeToEdit != null

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f)
                .clip(RoundedCornerShape(24.dp))
                .testTag("add_edit_perfume_dialog"),
            color = Slate900,
            border = androidx.compose.foundation.BorderStroke(1.dp, Slate800)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isEditing) "EDITAR PERFUME" else "NOVO PERFUME",
                        color = Amber400,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Slate800)
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Fechar", tint = Slate300, modifier = Modifier.size(18.dp))
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Scrollable Form Fields
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nome do Perfume *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("input_perfume_name"),
                        colors = fieldColors(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = brand,
                        onValueChange = { brand = it },
                        label = { Text("Marca / Casa *") },
                        placeholder = { Text("Ex: Dior, Chanel, Lattafa, Dumont...") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("input_perfume_brand"),
                        colors = fieldColors(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Family selection
                    Column {
                        Text("FAMÍLIA OLFATIVA", color = Slate400, fontSize = 10.sp, fontWeight = FontWeight.Black)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            AVAILABLE_FAMILIES.forEach { fam ->
                                val isSel = family.equals(fam, ignoreCase = true)
                                FilterChip(
                                    selected = isSel,
                                    onClick = { family = fam },
                                    label = { Text(fam, fontSize = 11.sp) },
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

                    // Olfactory pyramid fields
                    Text("NOTAS OLFATIVAS", color = Amber400, fontSize = 11.sp, fontWeight = FontWeight.Black)

                    OutlinedTextField(
                        value = topNotes,
                        onValueChange = { topNotes = it },
                        label = { Text("Notas de Saída (Top)") },
                        placeholder = { Text("Ex: Bergamota, Limão, Maçã...") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = fieldColors(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = heartNotes,
                        onValueChange = { heartNotes = it },
                        label = { Text("Notas de Coração (Heart)") },
                        placeholder = { Text("Ex: Lavanda, Jasmim, Rosa...") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = fieldColors(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = baseNotes,
                        onValueChange = { baseNotes = it },
                        label = { Text("Notas de Fundo (Base)") },
                        placeholder = { Text("Ex: Âmbar, Baunilha, Cedro, Patchouli...") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = fieldColors(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Price range
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = priceMin,
                            onValueChange = { priceMin = it },
                            label = { Text("Preço Mín (R$)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            colors = fieldColors(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedTextField(
                            value = priceMax,
                            onValueChange = { priceMax = it },
                            label = { Text("Preço Máx (R$)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            colors = fieldColors(),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    OutlinedTextField(
                        value = referenceName,
                        onValueChange = { referenceName = it },
                        label = { Text("Inspiração / Referência (Opcional)") },
                        placeholder = { Text("Ex: Inspirado no Aventus, Sauvage...") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = fieldColors(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = imageUrl,
                        onValueChange = { imageUrl = it },
                        label = { Text("URL da Imagem (Opcional)") },
                        placeholder = { Text("https://...") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = fieldColors(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        if (name.isBlank() || brand.isBlank()) return@Button
                        val pMin = priceMin.toDoubleOrNull() ?: 200.0
                        val pMax = priceMax.toDoubleOrNull() ?: 350.0

                        val allNotesStr = listOf(topNotes, heartNotes, baseNotes)
                            .filter { it.isNotBlank() }
                            .joinToString(" | ")

                        val perfume = if (isEditing) {
                            perfumeToEdit.copy(
                                name = name.trim(),
                                brand = brand.trim(),
                                family = family.trim(),
                                imageUrl = imageUrl.trim(),
                                priceMin = pMin,
                                priceMax = pMax,
                                topNotes = topNotes.trim(),
                                heartNotes = heartNotes.trim(),
                                baseNotes = baseNotes.trim(),
                                notes = allNotesStr,
                                referenceName = referenceName.trim()
                            )
                        } else {
                            PerfumeEntity(
                                id = "custom_${UUID.randomUUID()}",
                                name = name.trim(),
                                brand = brand.trim(),
                                family = family.trim(),
                                imageUrl = imageUrl.trim(),
                                priceMin = pMin,
                                priceMax = pMax,
                                topNotes = topNotes.trim(),
                                heartNotes = heartNotes.trim(),
                                baseNotes = baseNotes.trim(),
                                notes = allNotesStr,
                                referenceName = referenceName.trim(),
                                status = status,
                                tags = "DIA A DIA",
                                fixation = 7,
                                projection = 7,
                                isCustom = true
                            )
                        }

                        viewModel.savePerfume(perfume)
                    },
                    enabled = name.isNotBlank() && brand.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = Amber400, contentColor = Slate950),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("submit_perfume_button")
                ) {
                    Text(
                        text = if (isEditing) "SALVAR ALTERAÇÕES" else "ADICIONAR À COLEÇÃO",
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Amber400,
    unfocusedBorderColor = Slate800,
    focusedTextColor = Slate100,
    unfocusedTextColor = Slate100,
    focusedContainerColor = Slate950,
    unfocusedContainerColor = Slate950,
    focusedLabelColor = Amber400,
    unfocusedLabelColor = Slate400
)
