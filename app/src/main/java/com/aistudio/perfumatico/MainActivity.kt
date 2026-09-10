package com.aistudio.perfumatico

import android.os.Bundle
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aistudio.perfumatico.ui.components.PerfumaticoBottomNav
import com.aistudio.perfumatico.ui.components.PerfumaticoTopBar
import com.aistudio.perfumatico.ui.screens.*
import com.aistudio.perfumatico.ui.theme.Amber400
import com.aistudio.perfumatico.ui.theme.PerfumaticoTheme
import com.aistudio.perfumatico.ui.theme.Slate400
import com.aistudio.perfumatico.ui.theme.Slate950
import com.aistudio.perfumatico.ui.viewmodel.MainTab
import com.aistudio.perfumatico.ui.viewmodel.PerfumeViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: PerfumeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
                PerfumaticoApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun PerfumaticoApp(viewModel: PerfumeViewModel) {
    val isInitialized by viewModel.isInitialized.collectAsState()
    val currentTab by viewModel.currentTab.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val selectedPerfume by viewModel.selectedPerfume.collectAsState()
    val isAddEditOpen by viewModel.isAddEditOpen.collectAsState()
    val perfumeToEdit by viewModel.perfumeToEdit.collectAsState()
    val isProfileModalOpen by viewModel.isProfileModalOpen.collectAsState()
    val isAuthDialogOpen by viewModel.isAuthDialogOpen.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

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
                onAddClick = { viewModel.openAddEdit() },
                showAddButton = currentTab == MainTab.COLLECTION || currentTab == MainTab.CATALOG
            )
        },
        bottomBar = {
            PerfumaticoBottomNav(
                currentTab = currentTab,
                onTabSelected = { viewModel.setTab(it) }
            )
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
                MainTab.DISCOVER -> DiscoverScreen(viewModel = viewModel)
                MainTab.CATALOG -> CatalogScreen(viewModel = viewModel)
            }
        }

        // Modals / Dialogs
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
    }
}
