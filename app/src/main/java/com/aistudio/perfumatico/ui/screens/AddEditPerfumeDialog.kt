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
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.ui.text.input.KeyboardCapitalization
import com.aistudio.perfumatico.utils.unaccentAndNormalize
import com.aistudio.perfumatico.utils.levenshtein

import java.util.UUID
import androidx.compose.material.icons.filled.AutoAwesome
import org.json.JSONObject
import kotlinx.coroutines.launch

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
    var isAutoFilling by remember { mutableStateOf(false) }
    var fixation by remember { mutableStateOf(perfumeToEdit?.fixation ?: 7) }
    var projection by remember { mutableStateOf(perfumeToEdit?.projection ?: 7) }
    var aiFeedbackMessage by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Nome do Perfume *") },
                            singleLine = true,
                            modifier = Modifier.weight(1f).testTag("input_perfume_name"),
                            colors = fieldColors(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        
                        IconButton(
                            onClick = {
                                if (name.isNotBlank()) {
                                    isAutoFilling = true
                                    aiFeedbackMessage = null
                                    viewModel.autoFillPerfume(name) { jsonResult ->
                                        isAutoFilling = false
                                        try {
                                            val cleaned = jsonResult.replace("```json", "").replace("```", "").trim()
                                            val start = cleaned.indexOf('{')
                                            val end = cleaned.lastIndexOf('}')
                                            val jsonStr = if (start != -1 && end != -1 && end > start) {
                                                cleaned.substring(start, end + 1)
                                            } else cleaned
                                            
                                            val json = JSONObject(jsonStr)
                                            var filledSomething = false

                                            if (json.has("name") && json.getString("name").isNotBlank()) {
                                                val officialName = json.getString("name").trim()
                                                if (!officialName.equals(name, ignoreCase = true)) {
                                                    name = officialName
                                                }
                                                filledSomething = true
                                            }
                                            if (json.has("brand") && json.getString("brand").isNotBlank()) {
                                                brand = json.getString("brand").trim()
                                                filledSomething = true
                                            }
                                            if (json.has("family")) {
                                                val rawFam = json.getString("family").trim()
                                                val matched = AVAILABLE_FAMILIES.firstOrNull { 
                                                    it.equals(rawFam, ignoreCase = true) || rawFam.contains(it, ignoreCase = true)
                                                }
                                                if (matched != null) {
                                                    family = matched
                                                    filledSomething = true
                                                } else if (rawFam.isNotBlank()) {
                                                    family = rawFam
                                                    filledSomething = true
                                                }
                                            }

                                            fun extractNotes(key: String): String {
                                                if (!json.has(key)) return ""
                                                val opt = json.opt(key) ?: return ""
                                                return when (opt) {
                                                    is org.json.JSONArray -> {
                                                        (0 until opt.length())
                                                            .map { opt.optString(it).trim() }
                                                            .filter { it.isNotBlank() }
                                                            .joinToString(", ")
                                                    }
                                                    else -> opt.toString().trim()
                                                }
                                            }

                                            val tNotes = extractNotes("topNotes")
                                            if (tNotes.isNotBlank()) { topNotes = tNotes; filledSomething = true }

                                            val hNotes = extractNotes("heartNotes")
                                            if (hNotes.isNotBlank()) { heartNotes = hNotes; filledSomething = true }

                                            val bNotes = extractNotes("baseNotes")
                                            if (bNotes.isNotBlank()) { baseNotes = bNotes; filledSomething = true }

                                            if (json.has("fixation")) {
                                                val fix = json.optInt("fixation", 0)
                                                if (fix in 1..10) { fixation = fix; filledSomething = true }
                                            }
                                            if (json.has("projection")) {
                                                val proj = json.optInt("projection", 0)
                                                if (proj in 1..10) { projection = proj; filledSomething = true }
                                            }
                                            if (json.has("referenceName") && json.getString("referenceName").isNotBlank()) {
                                                referenceName = json.getString("referenceName").trim()
                                            }
                                            if (json.has("priceMin")) {
                                                val pMin = json.optInt("priceMin", 0)
                                                if (pMin > 0) priceMin = pMin.toString()
                                            }
                                            if (json.has("priceMax")) {
                                                val pMax = json.optInt("priceMax", 0)
                                                if (pMax > 0) priceMax = pMax.toString()
                                            }

                                            aiFeedbackMessage = if (filledSomething) {
                                                "✨ Pirâmide olfativa e dados preenchidos pela IA!"
                                            } else {
                                                "⚠️ Fragrância não detalhada. Você pode preencher manualmente abaixo."
                                            }
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                            aiFeedbackMessage = "⚠️ Não foi possível carregar as notas automaticamente. Preencha manualmente."
                                        }
                                    }
                                } else {
                                    aiFeedbackMessage = "Digite o nome do perfume antes de acionar a IA."
                                }
                            },
                            modifier = Modifier
                                .size(50.dp)
                                .background(Amber400, RoundedCornerShape(12.dp))
                        ) {
                            if (isAutoFilling) {
                                CircularProgressIndicator(color = Slate950, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Default.AutoAwesome, contentDescription = "Auto Preencher com IA", tint = Slate950)
                            }
                        }
                    }

                    if (aiFeedbackMessage != null) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (aiFeedbackMessage!!.startsWith("✨")) Amber400.copy(alpha = 0.15f) else Slate800,
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (aiFeedbackMessage!!.startsWith("✨")) Amber400.copy(alpha = 0.5f) else Slate700),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = aiFeedbackMessage!!,
                                color = if (aiFeedbackMessage!!.startsWith("✨")) Amber400 else Slate300,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }

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
                                fixation = fixation,
                                projection = projection,
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
                                fixation = fixation,
                                projection = projection,
                                notes = allNotesStr,
                                referenceName = referenceName.trim(),
                                status = status,
                                tags = "DIA A DIA",
                                
                                isCustom = true,
                                markedNewAt = System.currentTimeMillis()
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


private val commonNotes = listOf(
    "Bergamota", "Baunilha", "Âmbar", "Patchouli", "Lavanda", "Sândalo", "Cedro", "Vetiver", "Musk", 
    "Limão", "Fava Tonka", "Jasmim", "Couro", "Toranja", "Cardamomo", "Rosa", "Gerânio", "Maçã", 
    "Sálvia", "Especiarias", "Canela", "Gengibre", "Oud", "Pimenta Preta", "Açafrão", "Pimenta Rosa", 
    "Mandarina", "Notas Amadeiradas", "Íris", "Cítricos", "Madeiras", "Noz-moscada", "Almíscar", 
    "Musgo", "Incenso", "Flor de Laranjeira", "Pimenta", "Musgo de Carvalho", "Abacaxi", "Alecrim", 
    "Ambroxan", "Âmbar Cinzento", "Tabaco", "Notas Aquáticas", "Caramelo", "Hortelã", "Notas Florais", 
    "Laranja", "Benjoim", "Baunilha Bourbon", "Notas Verdes", "Menta", "Amêndoa", "Cassis", "Groselha Preta", 
    "Sálvia Esclareia", "Pralinê", "Café", "Aldeídos", "Madeira Guaiac", "Acorde Gourmand", "Madeira de Cedro", 
    "Manga", "Melão", "Cereja", "Ameixa", "Maçã Verde", "Folhas de Violeta", "Lichia", "Mel", "Framboesa", 
    "Coco", "Pêssego", "Ylang-Ylang", "Amora", "Orquídea", "Magnólia", "Lírio-do-Vale", "Peônia", "Eucalipto",
    "Notas Marinhas", "Castanha", "Zimbro", "Fumaça", "Resinas", "Trufa", "Tinta", "Pólvora", "Sal", "Areia",
    "Bambu", "Agave", "Leite", "Cannabis", "Vinho", "Gin", "Algodão", "Muguet", "Frangipani"
).sorted()

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NotesAutocompleteField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String
) {
    var expanded by remember { mutableStateOf(false) }
    var currentTerm by remember { mutableStateOf("") }
    
    LaunchedEffect(value) {
        val lastComma = value.lastIndexOf(",")
        currentTerm = if (lastComma != -1) {
            value.substring(lastComma + 1).trimStart()
        } else {
            value.trimStart()
        }
        expanded = currentTerm.length >= 2
    }
    
    val suggestions = remember(currentTerm) {
        if (currentTerm.length < 2) emptyList()
        else {
            val cleanTerm = currentTerm.unaccentAndNormalize()
            commonNotes.filter { note ->
                val cleanNote = note.unaccentAndNormalize()
                cleanNote.contains(cleanTerm) || levenshtein(cleanTerm, cleanNote) <= 2
            }.take(5)
        }
    }
    
    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            placeholder = { Text(placeholder) },
            modifier = Modifier.fillMaxWidth(),
            colors = fieldColors(),
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
        )
        
        AnimatedVisibility(visible = expanded && suggestions.isNotEmpty()) {
            FlowRow(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                suggestions.forEach { suggestion ->
                    AssistChip(
                        onClick = {
                            val lastComma = value.lastIndexOf(",")
                            val newValue = if (lastComma != -1) {
                                value.substring(0, lastComma + 1) + " " + suggestion + ", "
                            } else {
                                "$suggestion, "
                            }
                            onValueChange(newValue)
                            expanded = false
                        },
                        label = { Text(suggestion) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = Slate800,
                            labelColor = Amber400
                        ),
                        border = null
                    )
                }
            }
        }
    }
}
