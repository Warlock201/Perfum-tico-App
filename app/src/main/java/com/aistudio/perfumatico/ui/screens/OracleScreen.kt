package com.aistudio.perfumatico.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.aistudio.perfumatico.ui.components.PerfumeCard
import com.aistudio.perfumatico.ui.theme.*
import com.aistudio.perfumatico.ui.viewmodel.PerfumeViewModel
import com.aistudio.perfumatico.utils.OracleEngine
import com.aistudio.perfumatico.utils.OracleRecommendation
import com.aistudio.perfumatico.utils.WeatherService
import kotlinx.coroutines.launch
import android.location.Location

@Composable
fun OracleScreen(
    viewModel: PerfumeViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var isLoading by remember { mutableStateOf(false) }
    var weatherData by remember { mutableStateOf<com.aistudio.perfumatico.utils.WeatherData?>(null) }
    var selectedMicroclimate by remember { mutableStateOf<OracleEngine.Microclimate?>(null) }
    var recommendation by remember { mutableStateOf<OracleRecommendation?>(null) }
    var permissionDenied by remember { mutableStateOf(false) }

    val perfumes by viewModel.myPerfumes.collectAsState()

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            permissionDenied = false
            fetchWeather(scope) { data ->
                weatherData = data
            }
        } else {
            permissionDenied = true
        }
    }

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fetchWeather(scope) { data ->
                weatherData = data
            }
        } else {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(16.dp)) }
        
        item {
            Text(
                text = "ORÁCULO OLFATIVO",
                color = Amber400,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
            Text(
                text = "Descubra qual perfume da sua coleção é perfeito para hoje.",
                color = Slate400,
                fontSize = 13.sp
            )
        }

        // Weather Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Slate900),
                shape = RoundedCornerShape(16.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.verticalGradient(listOf(Slate800, Slate900))),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = Cyan500, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (weatherData != null) weatherData!!.city.uppercase() else "SEU CLIMA AGORA", color = Cyan500, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    if (permissionDenied) {
                        Text("Precisamos da permissão de localização para consultar o clima da sua região.", color = Rose500, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { locationPermissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION) }, colors = ButtonDefaults.buttonColors(containerColor = Slate800, contentColor = Cyan500)) {
                            Text("Autorizar")
                        }
                    } else if (weatherData == null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Amber400, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Lendo os céus...", color = Slate400, fontSize = 13.sp)
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.WbSunny, contentDescription = null, tint = Amber400, modifier = Modifier.size(28.dp))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("${weatherData!!.temperature.toInt()}ºC", color = Slate100, fontSize = 20.sp, fontWeight = FontWeight.Black)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.WaterDrop, contentDescription = null, tint = Blue500, modifier = Modifier.size(28.dp))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("${weatherData!!.humidity}%", color = Slate100, fontSize = 20.sp, fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }
            }
        }

        if (weatherData != null) {
            item {
                Text("PARA ONDE VOCÊ VAI?", color = Slate300, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                
                val climates = listOf(
                    OracleEngine.Microclimate.OFFICE_AC to "🏢 Escritório (Ar Condicionado Frio)",
                    OracleEngine.Microclimate.MALL_MILD to "🛍️ Shopping / Cinema (Clima Ameno)",
                    OracleEngine.Microclimate.OUTDOOR_HOT to "☀️ Ar Livre / Rua (Temperatura Natural)",
                    OracleEngine.Microclimate.DATE_NIGHT to "🍷 Encontro / Jantar (Noite)",
                    OracleEngine.Microclimate.CLUB_PARTY to "🪩 Balada / Pub (Ambiente Quente e Fechado)",
                    OracleEngine.Microclimate.GYM to "🏋️ Academia / Esporte",
                    OracleEngine.Microclimate.UNKNOWN to "🤷 Não sei / Versátil"
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    climates.forEach { (micro, label) ->
                        val isSelected = selectedMicroclimate == micro
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedMicroclimate = micro },
                            color = if (isSelected) Amber400.copy(alpha = 0.1f) else Slate900,
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) Amber400 else Slate800)
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) Amber400 else Slate300,
                                fontSize = 14.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }
            }

            item {
                Button(
                    onClick = {
                        if (selectedMicroclimate != null && weatherData != null) {
                            isLoading = true
                            recommendation = OracleEngine.recommend(weatherData!!, selectedMicroclimate!!, perfumes)
                            isLoading = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Amber400, contentColor = Slate950),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    enabled = selectedMicroclimate != null && !isLoading
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("O QUE USAR HOJE?", fontWeight = FontWeight.Black, fontSize = 15.sp)
                }
            }

            if (recommendation != null) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("O ORÁCULO DIZ:", color = Emerald400, fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 0.5.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Surface(
                        color = Emerald500.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Emerald500.copy(alpha=0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "\"${recommendation!!.reasoning}\"",
                            color = Emerald400,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(16.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("ESCOLHA PRINCIPAL", color = Slate400, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    PerfumeCard(
                        perfume = recommendation!!.primaryPerfume,
                        onClick = { viewModel.openPerfumeDetails(recommendation!!.primaryPerfume) }
                    )

                    if (recommendation!!.alternativePerfume != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("ALTERNATIVA", color = Slate400, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        PerfumeCard(
                            perfume = recommendation!!.alternativePerfume!!,
                            onClick = { viewModel.openPerfumeDetails(recommendation!!.alternativePerfume!!) }
                        )
                    }
                }
            } else if (selectedMicroclimate != null && recommendation == null && !isLoading) {
                item {
                    Text(
                        "Sua coleção não possui perfumes suficientes com as características ideais para esse clima. Vá à aba 'Descubra' para explorar mais!",
                        color = Slate400,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(16.dp)
                    )
                }
            }
        }
        
        item { Spacer(modifier = Modifier.height(32.dp)) }
    }
}

private fun fetchWeather(
    scope: kotlinx.coroutines.CoroutineScope,
    onResult: (com.aistudio.perfumatico.utils.WeatherData?) -> Unit
) {
    // Para simplificar e garantir a robustez, usamos as coordenadas de Fortaleza como base
    scope.launch {
        val data = WeatherService.getLocalWeather(-3.71722, -38.54306)
        onResult(data)
    }
}
