package com.aistudio.perfumatico.ui.screens
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import com.aistudio.perfumatico.utils.getNoteDrawableResId
import com.aistudio.perfumatico.utils.AccordsEngine
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.Image

import com.aistudio.perfumatico.utils.cleanNotePrefix
import com.aistudio.perfumatico.utils.normalizeNoteName
import com.aistudio.perfumatico.utils.getNoteImageUrl
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.style.TextAlign
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
    var status by remember { mutableStateOf(
        when (perfume.status) {
            "Já possuo", "Frasco", "Decant" -> "Já possuo"
            "Quero ter", "Pipeline" -> "Quero ter"
            "Desejo", "Desejos", "Wishlist" -> "Desejo"
            else -> "Nenhum"
        }
    ) }
    var userPreference by remember { mutableIntStateOf(perfume.userPreference) }
    val todaySotd by viewModel.todaySotd.collectAsState()
    val isTodaySotd = todaySotd?.perfumeId == perfume.id
    
    val noteImages by viewModel.noteImages.collectAsState()
    var editingNoteName by remember { mutableStateOf<String?>(null) }
    var editingNoteImageUrl by remember { mutableStateOf("") }

    val currentTags = remember(perfume.tags) {
        perfume.tags.split(",").map { it.trim() }.filter { it.isNotBlank() }.toMutableStateList()
    }

    var bottleSize by remember { 
        mutableStateOf(
            try {
                val arr = org.json.JSONArray(perfume.bottlesJson)
                val first = arr.get(0)
                if (first is org.json.JSONObject) {
                    first.getString("size")
                } else {
                    first.toString()
                }
            } catch(e: Exception) {
                if (perfume.bottlesJson.contains("50ml")) "50ml" else "100ml"
            }
        ) 
    }
    var bottleLevel by remember { 
        mutableFloatStateOf(
            try {
                val arr = org.json.JSONArray(perfume.bottlesJson)
                val first = arr.get(0)
                if (first is org.json.JSONObject) {
                    first.getInt("level").toFloat()
                } else {
                    100f
                }
            } catch(e: Exception) {
                100f
            }
        ) 
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

                    // Main Accords
                    val accords = remember(perfume) { AccordsEngine.calculateAccords(perfume) }
                    if (accords.isNotEmpty()) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Slate950),
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Slate800),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("ACORDES PRINCIPAIS", color = Amber400, fontSize = 11.sp, fontWeight = FontWeight.Black)
                                Spacer(modifier = Modifier.height(4.dp))
                                
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    val maxPercentage = accords.first().percentage.toFloat()
                                    accords.forEach { accord ->
                                        // Calculate relative width based on the highest percentage so it fills more space nicely
                                        val relativeWidth = accord.percentage / maxPercentage
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth(relativeWidth)
                                                .height(24.dp)
                                                .background(accord.color, RoundedCornerShape(4.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = accord.name.uppercase(),
                                                color = Slate950,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 8.dp)
                                            )
                                        }
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
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("PIRÂMIDE OLFATIVA", color = Amber400, fontSize = 11.sp, fontWeight = FontWeight.Black)
                                
                                // AI Button
                                var isCompletingWithAI by remember { mutableStateOf(false) }
                                if (perfume.topNotes.isBlank() && perfume.heartNotes.isBlank() && perfume.baseNotes.isBlank()) {
                                    if (isCompletingWithAI) {
                                        androidx.compose.material3.CircularProgressIndicator(
                                            color = Emerald400,
                                            modifier = Modifier.size(16.dp),
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Row(
                                            modifier = Modifier
                                                .clickable { 
                                                    isCompletingWithAI = true
                                                    viewModel.completeNotesWithAI(perfume) {
                                                        isCompletingWithAI = false
                                                    }
                                                }
                                                .background(Emerald500.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(Icons.Default.AutoAwesome, contentDescription = "AI", tint = Emerald400, modifier = Modifier.size(12.dp))
                                            Text("Completar com IA", color = Emerald400, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }

                            val onNoteClick: (String) -> Unit = { note ->
                                viewModel.toggleSearchNote(note.cleanNotePrefix().trim().lowercase())
                                viewModel.setTab(com.aistudio.perfumatico.ui.viewmodel.MainTab.CATALOG)
                                onDismiss()
                            }
                            val onNoteLongClick: (String) -> Unit = { note ->
                                val cleanName = note.cleanNotePrefix()
                                editingNoteName = cleanName
                                val savedUrl = noteImages[cleanName.normalizeNoteName()]
                                editingNoteImageUrl = if (savedUrl.isNullOrBlank()) {
                                    com.aistudio.perfumatico.data.local.DefaultNoteImages[cleanName.normalizeNoteName()] ?: ""
                                } else {
                                    savedUrl
                                }
                            }
                            if (perfume.topNotes.isNotBlank()) {
                                PyramidTier(tier = "Saída (Top)", notes = perfume.topNotes, color = Cyan500, noteImages = noteImages, onNoteClick = onNoteClick, onNoteLongClick = onNoteLongClick)
                            }
                            if (perfume.heartNotes.isNotBlank()) {
                                PyramidTier(tier = "Coração (Heart)", notes = perfume.heartNotes, color = Emerald400, noteImages = noteImages, onNoteClick = onNoteClick, onNoteLongClick = onNoteLongClick)
                            }
                            if (perfume.baseNotes.isNotBlank()) {
                                PyramidTier(tier = "Fundo (Base)", notes = perfume.baseNotes, color = Amber400, noteImages = noteImages, onNoteClick = onNoteClick, onNoteLongClick = onNoteLongClick)
                            }
                            if (perfume.topNotes.isBlank() && perfume.heartNotes.isBlank() && perfume.baseNotes.isBlank() && perfume.notes.isNotBlank()) {
                                Text(perfume.notes, color = Slate300, fontSize = 12.sp)
                            }
                        }
                    }

                    // Status Switcher
                    Column {
                        Text("STATUS NA COLEÇÃO", color = Slate400, fontSize = 10.sp, fontWeight = FontWeight.Black)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("Já possuo", "Quero ter", "Desejo", "Nenhum").forEach { st ->
                                val isSel = status == st
                                FilterChip(
                                    selected = isSel,
                                    onClick = { status = st },
                                    label = { Text(st, fontSize = 11.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = if (st == "Já possuo") Amber400 else if (st == "Quero ter") Cyan500 else if (st == "Desejo") Purple500 else Slate700,
                                        selectedLabelColor = if (st == "Nenhum" && isSel) Slate100 else Slate950,
                                        containerColor = Slate800,
                                        labelColor = Slate300
                                    ),
                                    border = null,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    // User Preference
                    Column {
                        Text("PREFERÊNCIA PESSOAL", color = Slate400, fontSize = 10.sp, fontWeight = FontWeight.Black)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val prefs = listOf(1 to "Amo", 2 to "Gosto", 3 to "Ok", 4 to "Não Gosto")
                            prefs.forEach { (value, label) ->
                                val isSel = userPreference == value
                                FilterChip(
                                    selected = isSel,
                                    onClick = { userPreference = if (isSel) 0 else value },
                                    label = { Text(label, fontSize = 10.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = when(value) {
                                            1 -> Rose500
                                            2 -> Amber400
                                            3 -> Emerald400
                                            else -> Slate600
                                        },
                                        selectedLabelColor = if (value == 4) Slate100 else Slate950,
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

                    // Volume & Level selector
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("FRASCO E VOLUME RESTANTE", color = Slate400, fontSize = 10.sp, fontWeight = FontWeight.Black)
                        
                        OutlinedTextField(
                            value = bottleSize,
                            onValueChange = { bottleSize = it },
                            label = { Text("Tamanho do frasco (ex: 100ml)", fontSize = 12.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Amber400,
                                unfocusedBorderColor = Slate800,
                                focusedLabelColor = Amber400,
                                unfocusedLabelColor = Slate400,
                                focusedTextColor = Slate200,
                                unfocusedTextColor = Slate200
                            ),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Slate800, RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Nível do Líquido", color = Slate300, fontSize = 12.sp)
                                Text("${bottleLevel.toInt()}%", color = Amber400, fontSize = 12.sp, fontWeight = FontWeight.Black)
                            }
                            androidx.compose.material3.Slider(
                                value = bottleLevel,
                                onValueChange = { bottleLevel = it },
                                valueRange = 0f..100f,
                                steps = 20,
                                colors = androidx.compose.material3.SliderDefaults.colors(
                                    thumbColor = Amber400,
                                    activeTrackColor = Amber400,
                                    inactiveTrackColor = Slate900
                                )
                            )
                            val levelLabel = when {
                                bottleLevel == 100f -> "Cheio"
                                bottleLevel > 75f -> "Quase Cheio"
                                bottleLevel > 40f -> "Pela Metade"
                                bottleLevel > 15f -> "Acabando"
                                else -> "No Fim / Seco"
                            }
                            Text(levelLabel, color = Slate400, fontSize = 11.sp, modifier = Modifier.align(Alignment.CenterHorizontally))
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
                
                // Parecido com...
                val similar = remember(perfume) { viewModel.getSimilarPerfumes(perfume) }
                if (similar.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("PARECIDO COM...", color = Amber400, fontSize = 11.sp, fontWeight = FontWeight.Black)
                    Spacer(modifier = Modifier.height(8.dp))
                    androidx.compose.foundation.lazy.LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(similar) { simPerfume ->
                            Column(
                                modifier = Modifier
                                    .width(80.dp)
                                    .clickable {
                                        onDismiss()
                                        viewModel.openPerfumeDetails(simPerfume)
                                    },
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                AsyncImage(
                                    model = simPerfume.imageUrl,
                                    contentDescription = simPerfume.name,
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Slate800),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = simPerfume.name,
                                    color = Slate100,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    maxLines = 2,
                                    lineHeight = 11.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                val isCurrentlyNew = perfume.markedNewAt > 0L && (System.currentTimeMillis() - perfume.markedNewAt) < 14L * 24 * 60 * 60 * 1000
                var markedNewToggle by remember { mutableStateOf(isCurrentlyNew) }
                var isSignature by remember { mutableStateOf(currentTags.any { it.equals("ASSINATURA", ignoreCase = true) }) }

                // Marcar como Novo Switch Card
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { markedNewToggle = !markedNewToggle }
                        .background(if (markedNewToggle) Emerald500.copy(alpha = 0.15f) else Slate800, RoundedCornerShape(12.dp))
                        .border(1.dp, if (markedNewToggle) Emerald500 else Slate700, RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.NewReleases,
                            contentDescription = null,
                            tint = if (markedNewToggle) Emerald500 else Slate400,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                "Recém-Adquirido (Novo)",
                                color = if (markedNewToggle) Emerald500 else Slate200,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Destacar como novidade na coleção",
                                color = Slate400,
                                fontSize = 10.sp
                            )
                        }
                    }
                    Switch(
                        checked = markedNewToggle,
                        onCheckedChange = { markedNewToggle = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Emerald500,
                            checkedTrackColor = Emerald500.copy(alpha = 0.3f),
                            uncheckedThumbColor = Slate400,
                            uncheckedTrackColor = Slate700
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Perfume Assinatura Switch Card
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { 
                            isSignature = !isSignature
                            if (isSignature) {
                                if (!currentTags.any { it.equals("ASSINATURA", ignoreCase = true) }) {
                                    currentTags.add("ASSINATURA")
                                }
                            } else {
                                currentTags.removeAll { it.equals("ASSINATURA", ignoreCase = true) }
                            }
                        }
                        .background(if (isSignature) Amber400.copy(alpha = 0.15f) else Slate800, RoundedCornerShape(12.dp))
                        .border(1.dp, if (isSignature) Amber400 else Slate700, RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = if (isSignature) Amber400 else Slate400,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                "Perfume Assinatura ⭐",
                                color = if (isSignature) Amber400 else Slate200,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Sua fragrância marca registrada",
                                color = Slate400,
                                fontSize = 10.sp
                            )
                        }
                    }
                    Switch(
                        checked = isSignature,
                        onCheckedChange = { checked ->
                            isSignature = checked
                            if (checked) {
                                if (!currentTags.any { it.equals("ASSINATURA", ignoreCase = true) }) {
                                    currentTags.add("ASSINATURA")
                                }
                            } else {
                                currentTags.removeAll { it.equals("ASSINATURA", ignoreCase = true) }
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Amber400,
                            checkedTrackColor = Amber400.copy(alpha = 0.3f),
                            uncheckedThumbColor = Slate400,
                            uncheckedTrackColor = Slate700
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))


                Spacer(modifier = Modifier.height(14.dp))

                // Action buttons: Usar Hoje & Salvar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (isTodaySotd) {
                        Button(
                            onClick = {
                                viewModel.registerSotd(perfume, "Reafirmado como fragrância de hoje!")
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Amber400, contentColor = Slate950),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.WbSunny, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Usando Hoje ☀️", fontWeight = FontWeight.Black, fontSize = 13.sp)
                        }
                    } else {
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
                            Text("Usar Hoje ☀️", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }

                    Button(
                        onClick = {
                            val newMarkedNewAt = if (markedNewToggle) {
                                if (perfume.markedNewAt > 0L) perfume.markedNewAt else System.currentTimeMillis()
                            } else {
                                0L
                            }
                            val updated = perfume.copy(
                                status = status,
                                userPreference = userPreference,
                                fixation = fixation.toInt(),
                                projection = projection.toInt(),
                                tags = currentTags.joinToString(", "),
                                personalNotes = personalNotes.trim(),
                                bottlesJson = "[{\"size\":\"${bottleSize.trim()}\", \"level\":${bottleLevel.toInt()}}]",
                                markedNewAt = newMarkedNewAt
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

    if (editingNoteName != null) {
        AlertDialog(
            onDismissRequest = { editingNoteName = null },
            title = { Text("Editar Imagem da Nota", color = Slate50, fontSize = 18.sp) },
            text = {
                Column {
                    Text("Você está editando a imagem para a nota: $editingNoteName.", color = Slate300, fontSize = 12.sp, modifier = Modifier.padding(bottom = 12.dp))
                    OutlinedTextField(
                        value = editingNoteImageUrl,
                        onValueChange = { editingNoteImageUrl = it },
                        label = { Text("URL da Imagem") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Slate50,
                            unfocusedTextColor = Slate200,
                            focusedBorderColor = Cyan500,
                            unfocusedBorderColor = Slate600
                        )
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.saveNoteImage(editingNoteName!!.normalizeNoteName(), editingNoteImageUrl)
                    editingNoteName = null
                }) {
                    Text("Salvar", color = Cyan500)
                }
            },
            dismissButton = {
                TextButton(onClick = { editingNoteName = null }) {
                    Text("Cancelar", color = Slate400)
                }
            },
            containerColor = Slate800
        )
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@Composable
fun PyramidTier(
    tier: String,
    notes: String,
    color: Color,
    noteImages: Map<String, String>,
    onNoteClick: (String) -> Unit,
    onNoteLongClick: (String) -> Unit = {}
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = tier,
            color = color,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
        val noteList = notes.split(",", "|").map { it.cleanNotePrefix() }.filter { it.isNotBlank() }
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            noteList.forEach { noteName ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .background(Slate800, RoundedCornerShape(12.dp))
                        .clip(RoundedCornerShape(12.dp))
                        .combinedClickable(
                            onClick = { onNoteClick(noteName) },
                            onLongClick = { onNoteLongClick(noteName) }
                        )
                        .padding(8.dp)
                        .width(72.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Slate700),
                        contentAlignment = Alignment.Center
                    ) {
                        val context = LocalContext.current
                        val resId = context.getNoteDrawableResId(noteName)
                        if (resId != 0) {
                            Image(
                                painter = painterResource(id = resId),
                                contentDescription = noteName,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            val customUrl = noteImages[noteName.normalizeNoteName()]
                            val finalUrl = if (customUrl.isNullOrBlank()) {
                                com.aistudio.perfumatico.data.local.DefaultNoteImages[noteName.normalizeNoteName()] 
                                ?: noteName.getNoteImageUrl()
                            } else customUrl
                            
                            if (finalUrl.isBlank()) {
                                Icon(
                                    imageVector = Icons.Default.Spa,
                                    contentDescription = null,
                                    tint = Emerald400,
                                    modifier = Modifier.size(24.dp)
                                )
                            } else {
                                coil.compose.SubcomposeAsyncImage(
                                    model = finalUrl,
                                    contentDescription = noteName,
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
                    Text(
                        text = noteName,
                        color = Slate200,
                        fontSize = 10.sp,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        lineHeight = 12.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
