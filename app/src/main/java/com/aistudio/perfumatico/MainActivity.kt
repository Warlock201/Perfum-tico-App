package com.aistudio.perfumatico

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import coil.ImageLoader
import coil.Coil
import okhttp3.OkHttpClient
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import com.aistudio.perfumatico.data.local.PerfumeEntity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.isImeVisible
import com.aistudio.perfumatico.ui.components.PerfumaticoBottomNav
import com.aistudio.perfumatico.ui.components.PerfumaticoTopBar
import com.aistudio.perfumatico.ui.screens.*
import com.aistudio.perfumatico.ui.theme.Amber400
import com.aistudio.perfumatico.ui.theme.PerfumaticoTheme
import com.aistudio.perfumatico.ui.theme.Slate400
import com.aistudio.perfumatico.ui.theme.Slate950
import com.aistudio.perfumatico.ui.viewmodel.MainTab
import com.aistudio.perfumatico.ui.viewmodel.PerfumeViewModel
import com.aistudio.perfumatico.ui.viewmodel.ChatViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: PerfumeViewModel by viewModels()
    private val chatViewModel: ChatViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // PROTEÇÃO CONTRA PRINTS (Ativado apenas em Release para não quebrar o emulador de preview)
        if (!BuildConfig.DEBUG) {
            window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
        }
        enableEdgeToEdge()
        
        val imageLoader = ImageLoader.Builder(this)
            .okHttpClient {
                OkHttpClient.Builder()
                    .connectTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                    .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                    .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                    .addInterceptor { chain ->
                        val original = chain.request()
                        val request = original.newBuilder()
                            .header("User-Agent", "Mozilla/5.0 Perfumatico/1.0")
                            .build()
                        chain.proceed(request)
                    }
                    .build()
            }
            .build()
        Coil.setImageLoader(imageLoader)
        
        setContent {
            PerfumaticoTheme {
                PerfumaticoApp(viewModel = viewModel, chatViewModel = chatViewModel)
            }
        }
    }
}

@Composable
fun PerfumaticoApp(viewModel: PerfumeViewModel, chatViewModel: ChatViewModel) {
    val isInitialized by viewModel.isInitialized.collectAsState()
    val currentTab by viewModel.currentTab.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val selectedPerfume by viewModel.selectedPerfume.collectAsState()
    val isAddChooserOpen by viewModel.isAddChooserOpen.collectAsState()
    val isAddEditOpen by viewModel.isAddEditOpen.collectAsState()
    val perfumeToEdit by viewModel.perfumeToEdit.collectAsState()
    val isProfileModalOpen by viewModel.isProfileModalOpen.collectAsState()
    val isAuthDialogOpen by viewModel.isAuthDialogOpen.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val updateInfo by viewModel.updateInfo.collectAsState()
    val noUpdateAvailable by viewModel.noUpdateAvailable.collectAsState()

    val context = LocalContext.current
    LaunchedEffect(chatViewModel) {
        chatViewModel.addPerfumeEvent.collect { (name, brand, status) ->
            val newPerfume = PerfumeEntity(
                id = "my_${System.currentTimeMillis()}",
                name = name,
                brand = brand,
                status = status,
                imageUrl = "",
                notes = "",
                family = ""
            )
            viewModel.savePerfume(newPerfume)
            Toast.makeText(context, "$name adicionado em $status!", Toast.LENGTH_SHORT).show()
        }
    }


    if (!isInitialized) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Slate950),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CircularProgressIndicator(color = Amber400)
                Text(
                    text = "CARREGANDO ACERVO PERFUMÁTICO...",
                    color = Slate400,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        }
        return
    }

    Scaffold(
        topBar = {
            PerfumaticoTopBar(
                userName = currentUser?.displayName ?: (userProfile?.displayName ?: "Colecionador"),
                isCloudConnected = currentUser != null,
                isAdmin = viewModel.isAdmin,
                onProfileClick = { viewModel.openProfileModal() },
                onAddClick = { viewModel.openAddChooser() },
                showAddButton = false
            )
        },
        bottomBar = {
            @OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
            val isImeVisible = WindowInsets.isImeVisible
            if (!isImeVisible) {
            PerfumaticoBottomNav(
                currentTab = currentTab,
                onTabSelected = { viewModel.setTab(it) }
            )
            }
        },
        containerColor = Slate950,
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                MainTab.COLLECTION -> MyPerfumesScreen(viewModel = viewModel)
                MainTab.DASHBOARD -> DashboardScreen(viewModel = viewModel)
                MainTab.DISCOVER, MainTab.CATALOG -> CatalogScreen(viewModel = viewModel)
                MainTab.ORACLE, MainTab.CHATBOT -> com.aistudio.perfumatico.ui.screens.OracleScreen(viewModel = viewModel, chatViewModel = chatViewModel)
            }
        }

        // Modals / Dialogs
        if (isAddChooserOpen) {
            com.aistudio.perfumatico.ui.components.AddPerfumeChooserDialog(
                onDismiss = { viewModel.closeAddChooser() },
                onAddNewWithAi = {
                    viewModel.closeAddChooser()
                    viewModel.openAddEdit(null)
                },
                onSelectFromCatalog = {
                    viewModel.navigateToCatalog()
                }
            )
        }

        selectedPerfume?.let { perfume ->
            PerfumeDetailDialog(
                perfume = perfume,
                viewModel = viewModel,
                onDismiss = { viewModel.closePerfumeDetails() }
            )
        }

        if (isAddEditOpen) {
            AddEditPerfumeDialog(
                perfumeToEdit = perfumeToEdit,
                viewModel = viewModel,
                onDismiss = { viewModel.closeAddEdit() }
            )
        }

        if (isProfileModalOpen) {
            ProfileSotdDialog(
                viewModel = viewModel,
                onDismiss = { viewModel.closeProfileModal() }
            )
        }

        updateInfo?.let { info ->
            AlertDialog(
                onDismissRequest = { viewModel.dismissUpdate() },
                containerColor = Slate950,
                titleContentColor = Amber400,
                textContentColor = Slate400,
                title = { Text(text = "Nova Atualização Disponível!") },
                text = {
                    Column {
                        Text("Versão: ${info.latestVersionName}")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(info.releaseNotes)
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { viewModel.downloadUpdate(info.apkUrl) },
                        colors = ButtonDefaults.buttonColors(containerColor = Amber400, contentColor = Slate950)
                    ) {
                        Text("Baixar e Instalar", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.dismissUpdate() }) {
                        Text("Mais tarde", color = Slate400)
                    }
                }
            )
        }

        if (noUpdateAvailable) {
            AlertDialog(
                onDismissRequest = { viewModel.dismissNoUpdateDialog() },
                containerColor = Slate950,
                titleContentColor = Amber400,
                textContentColor = Slate400,
                title = { Text(text = "App Atualizado") },
                text = { Text("Você já está na versão mais recente do Perfumático.") },
                confirmButton = {
                    Button(
                        onClick = { viewModel.dismissNoUpdateDialog() },
                        colors = ButtonDefaults.buttonColors(containerColor = Amber400, contentColor = Slate950)
                    ) {
                        Text("OK", fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    }
}
