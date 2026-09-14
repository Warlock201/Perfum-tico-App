package com.aistudio.perfumatico.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.aistudio.perfumatico.data.local.PerfumeEntity
import com.aistudio.perfumatico.data.remote.GeminiService
import com.aistudio.perfumatico.ui.components.ApiKeyConfigDialog
import com.aistudio.perfumatico.ui.components.PerfumeCard
import com.aistudio.perfumatico.ui.theme.*
import com.aistudio.perfumatico.ui.viewmodel.PerfumeViewModel
import com.aistudio.perfumatico.utils.OracleEngine
import com.aistudio.perfumatico.utils.OracleRecommendation
import com.aistudio.perfumatico.utils.WeatherData
import com.aistudio.perfumatico.utils.WeatherService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class OracleAiSection {
    WEATHER_ORACLE,
    SOMMELIER_CHAT
}

enum class OracleTab {
    PLACE,
    TEMPERATURE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OracleScreen(
    viewModel: PerfumeViewModel,
    chatViewModel: com.aistudio.perfumatico.ui.viewmodel.ChatViewModel? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var aiSection by remember { mutableStateOf(OracleAiSection.WEATHER_ORACLE) }
    var activeTab by remember { mutableStateOf(OracleTab.PLACE) }
    var isLoading by remember { mutableStateOf(false) }
    var weatherData by remember { mutableStateOf<WeatherData?>(null) }
    var permissionDenied by remember { mutableStateOf(false) }
    var showApiKeyDialog by remember { mutableStateOf(false) }

    // Place state
    val placeOptions = listOf(
        "🏢 Escritório (Ar Condicionado)",
        "🛍️ Shopping / Cinema (Ameno)",
        "☀️ Ao Ar Livre / Rua",
        "🍷 Encontro / Jantar Noturno",
        "🪩 Balada / Festa Animada",
        "🏋️ Academia / Treino",
        "🌴 Praia / Calor Extremo",
        "🏠 Casa / Home Office / Relaxar"
    )
    var selectedPlace by remember { mutableStateOf(placeOptions[0]) }
    var customPlaceText by remember { mutableStateOf("") }

    // Temperature state
    val tempOptions = listOf(
        "❄️ Frio Intenso (< 16°C)",
        "🌤️ Ameno / Fresco (17°C a 23°C)",
        "☀️ Calor Moderado (24°C a 29°C)",
        "🔥 Calor Extremo (30°C+)",
        "🤷 Não sei / Ambiente Variável"
    )
    var selectedTempOption by remember { mutableStateOf(tempOptions[1]) }
    var customTempText by remember { mutableStateOf("") }

    var recommendation by remember { mutableStateOf<OracleRecommendation?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val perfumes by viewModel.myPerfumes.collectAsState()
    val availablePerfumes = remember(perfumes) {
        perfumes.filter { it.status == "Já possuo" || it.status == "Frasco" || it.status == "Decant" }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            permissionDenied = false
            fetchWeather(scope) { data ->
                weatherData = data
                if (data != null && selectedTempOption.startsWith("🤷")) {
                    selectedTempOption = "🌤️ Ameno / Fresco (17°C a 23°C)"
                }
            }
        } else {
            permissionDenied = true
            fetchWeather(scope) { data -> weatherData = data }
        }
    }

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fetchWeather(scope) { data -> weatherData = data }
        } else {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Slate950)
    ) {
        if (chatViewModel != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Slate900)
                        .border(1.dp, Slate800, RoundedCornerShape(12.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Button(
                        onClick = { aiSection = OracleAiSection.WEATHER_ORACLE },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (aiSection == OracleAiSection.WEATHER_ORACLE) Amber400 else Color.Transparent,
                            contentColor = if (aiSection == OracleAiSection.WEATHER_ORACLE) Slate950 else Slate400
                        ),
                        shape = RoundedCornerShape(8.dp),
                        elevation = null,
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("ORÁCULO CLIMA", fontSize = 11.sp, fontWeight = FontWeight.Black)
                    }

                    Button(
                        onClick = { aiSection = OracleAiSection.SOMMELIER_CHAT },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (aiSection == OracleAiSection.SOMMELIER_CHAT) Amber400 else Color.Transparent,
                            contentColor = if (aiSection == OracleAiSection.SOMMELIER_CHAT) Slate950 else Slate400
                        ),
                        shape = RoundedCornerShape(8.dp),
                        elevation = null,
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.VoiceChat, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("SOMMELIER IA", fontSize = 11.sp, fontWeight = FontWeight.Black)
                    }
                }

                IconButton(
                    onClick = { showApiKeyDialog = true },
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Slate900)
                        .border(1.dp, Slate800, RoundedCornerShape(12.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.Key,
                        contentDescription = "Configurar Chave Gemini",
                        tint = Amber400,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        if (showApiKeyDialog) {
            ApiKeyConfigDialog(onDismiss = { showApiKeyDialog = false })
        }

        if (aiSection == OracleAiSection.SOMMELIER_CHAT && chatViewModel != null) {
            ChatScreen(viewModel = chatViewModel)
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
        item { Spacer(modifier = Modifier.height(8.dp)) }

        // Header Title
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "ORÁCULO INTELIGENTE",
                    color = Amber400,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "A IA analisa o local e a temperatura para indicar a fragrância perfeita da sua coleção.",
                    color = Slate400,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            }
        }

        // Live Weather Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Slate900),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Slate800),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = Cyan500, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (weatherData != null) weatherData!!.city.uppercase() else "CLIMA DA SUA REGIÃO",
                                color = Cyan500,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        }
                        IconButton(
                            onClick = {
                                fetchWeather(scope) { weatherData = it }
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Atualizar Clima", tint = Slate400, modifier = Modifier.size(16.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (weatherData != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.WbSunny, contentDescription = null, tint = Amber400, modifier = Modifier.size(26.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("${weatherData!!.temperature.toInt()}ºC", color = Slate100, fontSize = 22.sp, fontWeight = FontWeight.Black)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.WaterDrop, contentDescription = null, tint = Blue500, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("${weatherData!!.humidity}% Umidade", color = Slate300, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Amber400, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Consultando clima local...", color = Slate400, fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        // Segmented Tabs: 1. LUGAR e 2. TEMPERATURA
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Slate900)
                    .border(1.dp, Slate800, RoundedCornerShape(12.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Tab Lugar
                Button(
                    onClick = { activeTab = OracleTab.PLACE },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (activeTab == OracleTab.PLACE) Amber400 else Color.Transparent,
                        contentColor = if (activeTab == OracleTab.PLACE) Slate950 else Slate300
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(vertical = 10.dp)
                ) {
                    Icon(Icons.Default.Place, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("1. Lugar", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                // Tab Temperatura
                Button(
                    onClick = { activeTab = OracleTab.TEMPERATURE },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (activeTab == OracleTab.TEMPERATURE) Amber400 else Color.Transparent,
                        contentColor = if (activeTab == OracleTab.TEMPERATURE) Slate950 else Slate300
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(vertical = 10.dp)
                ) {
                    Icon(Icons.Default.Thermostat, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("2. Temperatura", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }

        // Content of Active Tab
        when (activeTab) {
            OracleTab.PLACE -> {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Slate900),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, Slate800),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Text(
                                text = "ONDE VOCÊ VAI OU ESTÁ?",
                                color = Amber400,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp
                            )

                            // Quick choices
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                placeOptions.forEach { opt ->
                                    val isSelected = selectedPlace == opt
                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { selectedPlace = opt },
                                        color = if (isSelected) Amber400.copy(alpha = 0.15f) else Slate800,
                                        shape = RoundedCornerShape(10.dp),
                                        border = if (isSelected) BorderStroke(1.dp, Amber400) else BorderStroke(1.dp, Slate700)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            RadioButton(
                                                selected = isSelected,
                                                onClick = { selectedPlace = opt },
                                                colors = RadioButtonDefaults.colors(
                                                    selectedColor = Amber400,
                                                    unselectedColor = Slate500
                                                )
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = opt,
                                                color = if (isSelected) Amber400 else Slate200,
                                                fontSize = 13.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            // Free text input for place
                            Text(
                                text = "OU ESCREVA DETALHES DO LOCAL (PARA A IA):",
                                color = Slate400,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )

                            OutlinedTextField(
                                value = customPlaceText,
                                onValueChange = { customPlaceText = it },
                                placeholder = {
                                    Text("Ex: Sala de reunião fechada com ar condicionado a 20°C, festa de formatura, etc.", color = Slate500, fontSize = 12.sp)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Amber400,
                                    unfocusedBorderColor = Slate700,
                                    focusedContainerColor = Slate950,
                                    unfocusedContainerColor = Slate950,
                                    focusedTextColor = Slate100,
                                    unfocusedTextColor = Slate100
                                ),
                                maxLines = 3
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(
                                    onClick = { activeTab = OracleTab.TEMPERATURE }
                                ) {
                                    Text("Avançar para Temperatura", color = Amber400, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(Icons.Default.ArrowForward, contentDescription = null, tint = Amber400, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }

            OracleTab.TEMPERATURE -> {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Slate900),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, Slate800),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Text(
                                text = "QUAL A TEMPERATURA / CLIMA?",
                                color = Amber400,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp
                            )

                            // If detected weather is available, show a shortcut
                            if (weatherData != null) {
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            val t = weatherData!!.temperature.toInt()
                                            selectedTempOption = when {
                                                t < 16 -> tempOptions[0]
                                                t in 16..23 -> tempOptions[1]
                                                t in 24..29 -> tempOptions[2]
                                                else -> tempOptions[3]
                                            }
                                            customTempText = "Temperatura detectada de ${t}ºC em ${weatherData!!.city}, umidade de ${weatherData!!.humidity}%"
                                        },
                                    color = Emerald500.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, Emerald500.copy(alpha = 0.3f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.CloudQueue, contentDescription = null, tint = Emerald400, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Usar clima detectado (${weatherData!!.temperature.toInt()}°C em ${weatherData!!.city})",
                                            color = Emerald400,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            // Temperature options including "Não sei"
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                tempOptions.forEach { opt ->
                                    val isSelected = selectedTempOption == opt
                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { selectedTempOption = opt },
                                        color = if (isSelected) Amber400.copy(alpha = 0.15f) else Slate800,
                                        shape = RoundedCornerShape(10.dp),
                                        border = if (isSelected) BorderStroke(1.dp, Amber400) else BorderStroke(1.dp, Slate700)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            RadioButton(
                                                selected = isSelected,
                                                onClick = { selectedTempOption = opt },
                                                colors = RadioButtonDefaults.colors(
                                                    selectedColor = Amber400,
                                                    unselectedColor = Slate500
                                                )
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = opt,
                                                color = if (isSelected) Amber400 else Slate200,
                                                fontSize = 13.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            // Free text input for temperature/climate
                            Text(
                                text = "OU ESCREVA A SENSAÇÃO TÉRMICA (PARA A IA):",
                                color = Slate400,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )

                            OutlinedTextField(
                                value = customTempText,
                                onValueChange = { customTempText = it },
                                placeholder = {
                                    Text("Ex: Chuva forte com vento gelado, ou 28°C mas muito abafado, ar condicionado no máximo...", color = Slate500, fontSize = 12.sp)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Amber400,
                                    unfocusedBorderColor = Slate700,
                                    focusedContainerColor = Slate950,
                                    unfocusedContainerColor = Slate950,
                                    focusedTextColor = Slate100,
                                    unfocusedTextColor = Slate100
                                ),
                                maxLines = 3
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Start
                            ) {
                                TextButton(
                                    onClick = { activeTab = OracleTab.PLACE }
                                ) {
                                    Icon(Icons.Default.ArrowBack, contentDescription = null, tint = Amber400, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Voltar para Lugar", color = Amber400, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Summary of selections before asking
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Slate900,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Slate800)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "RESUMO DA CONSULTA",
                        color = Slate400,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("📍 Lugar: ", color = Slate400, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = if (customPlaceText.isNotBlank()) "$selectedPlace ($customPlaceText)" else selectedPlace,
                            color = Slate200,
                            fontSize = 12.sp
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🌡️ Clima: ", color = Slate400, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = if (customTempText.isNotBlank()) "$selectedTempOption ($customTempText)" else selectedTempOption,
                            color = Slate200,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // Big Action Button: CONSULTAR ORÁCULO COM IA
        item {
            Button(
                onClick = {
                    if (availablePerfumes.isEmpty()) {
                        errorMessage = "Você ainda não tem perfumes com status 'Já possuo' na sua coleção. Adicione seus perfumes primeiro!"
                        return@Button
                    }

                    isLoading = true
                    errorMessage = null

                    scope.launch {
                        try {
                            // 1. Ask Gemini AI with detailed user place and temperature context
                            val aiResult = GeminiService.consultOracleAI(
                                place = selectedPlace,
                                placeCustomDetails = customPlaceText,
                                temperature = selectedTempOption,
                                temperatureCustomDetails = customTempText,
                                perfumes = availablePerfumes
                            )

                            if (aiResult != null) {
                                val matchedPerfume = availablePerfumes.find { it.id == aiResult.first }
                                    ?: availablePerfumes.find { it.name.contains(aiResult.first, ignoreCase = true) }
                                    ?: availablePerfumes.first()

                                val alternative = availablePerfumes.find { it.id != matchedPerfume.id }

                                recommendation = OracleRecommendation(
                                    primaryPerfume = matchedPerfume,
                                    alternativePerfume = alternative,
                                    reasoning = aiResult.second
                                )
                            } else {
                                // Fallback to OracleEngine
                                val micro = when {
                                    selectedPlace.contains("Escritório", true) -> OracleEngine.Microclimate.OFFICE_AC
                                    selectedPlace.contains("Shopping", true) -> OracleEngine.Microclimate.MALL_MILD
                                    selectedPlace.contains("Ar Livre", true) || selectedPlace.contains("Praia", true) -> OracleEngine.Microclimate.OUTDOOR_HOT
                                    selectedPlace.contains("Encontro", true) -> OracleEngine.Microclimate.DATE_NIGHT
                                    selectedPlace.contains("Balada", true) -> OracleEngine.Microclimate.CLUB_PARTY
                                    selectedPlace.contains("Academia", true) -> OracleEngine.Microclimate.GYM
                                    else -> OracleEngine.Microclimate.UNKNOWN
                                }

                                val estimatedTemp = when {
                                    selectedTempOption.contains("< 16") -> 14f
                                    selectedTempOption.contains("17°C a 23") -> 20f
                                    selectedTempOption.contains("24°C a 29") -> 26f
                                    selectedTempOption.contains("30°C+") -> 32f
                                    else -> weatherData?.temperature ?: 24f
                                }

                                val fakeWeather = weatherData ?: WeatherData(
                                    temperature = estimatedTemp,
                                    humidity = 60,
                                    isDay = true,
                                    city = "Ambiente"
                                )

                                val engineResult = OracleEngine.recommend(fakeWeather, micro, availablePerfumes)
                                if (engineResult != null) {
                                    recommendation = engineResult
                                } else {
                                    errorMessage = "Nenhum perfume da sua coleção foi compatível com esses requisitos exatos. Experimente selecionar outro clima ou adicione fragrâncias versáteis à coleção!"
                                }
                            }
                        } catch (e: Exception) {
                            errorMessage = "Erro ao consultar o Oráculo: ${e.message}"
                        } finally {
                            isLoading = false
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Amber400,
                    contentColor = Slate950
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Slate950, strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("A IA ESTÁ CONSULTANDO O ORÁCULO...", fontWeight = FontWeight.Black, fontSize = 13.sp)
                } else {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("CONSULTAR ORÁCULO COM IA", fontWeight = FontWeight.Black, fontSize = 14.sp)
                }
            }
        }

        // Error message if any
        if (errorMessage != null) {
            item {
                Surface(
                    color = Rose500.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Rose500.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = errorMessage!!,
                        color = Rose500,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(14.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Result presentation
        if (recommendation != null) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "RESPOSTA DO ORÁCULO:",
                        color = Emerald400,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    )
                    Surface(
                        color = Emerald500.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(1.dp, Emerald400)
                    ) {
                        Text(
                            text = "IA SOMMELIER",
                            color = Emerald400,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Reasoning card
            item {
                Surface(
                    color = Emerald500.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, Emerald500.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "\"${recommendation!!.reasoning}\"",
                            color = Emerald400,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            lineHeight = 20.sp,
                            textAlign = TextAlign.Start
                        )
                    }
                }
            }

            // Primary Perfume
            item {
                Text(
                    text = "FRAGRÂNCIA IDEAL",
                    color = Slate400,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                PerfumeCard(
                    perfume = recommendation!!.primaryPerfume,
                    onClick = { viewModel.openPerfumeDetails(recommendation!!.primaryPerfume) },
                    onSotdClick = {
                        viewModel.registerSotd(recommendation!!.primaryPerfume, "Indicado pelo Oráculo para $selectedPlace")
                    }
                )
            }

            // Alternative Perfume if present
            if (recommendation!!.alternativePerfume != null) {
                item {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "SEGUNDA OPÇÃO (ALTERNATIVA)",
                        color = Slate400,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    PerfumeCard(
                        perfume = recommendation!!.alternativePerfume!!,
                        onClick = { viewModel.openPerfumeDetails(recommendation!!.alternativePerfume!!) },
                        onSotdClick = {
                            viewModel.registerSotd(recommendation!!.alternativePerfume!!, "Segunda opção do Oráculo para $selectedPlace")
                        }
                    )
                }
            }
        }
    }
}
}
}

private fun fetchWeather(
    scope: kotlinx.coroutines.CoroutineScope,
    onResult: (WeatherData?) -> Unit
) {
    scope.launch {
        try {
            val data = withContext(Dispatchers.IO) {
                WeatherService.getLocalWeather(-3.71722, -38.54306)
            }
            onResult(data)
        } catch (e: Exception) {
            onResult(null)
        }
    }
}
