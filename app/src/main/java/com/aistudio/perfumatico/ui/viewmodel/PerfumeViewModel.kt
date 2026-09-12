package com.aistudio.perfumatico.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.aistudio.perfumatico.data.local.PerfumeEntity
import com.aistudio.perfumatico.data.local.SotdEntity
import com.aistudio.perfumatico.data.local.UserProfileEntity
import com.aistudio.perfumatico.data.remote.GeminiService
import com.aistudio.perfumatico.data.model.OlfactoryNote
import com.aistudio.perfumatico.data.repository.PerfumeRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import java.text.SimpleDateFormat
import java.util.*

enum class MainTab {
    COLLECTION,
    DASHBOARD,
    DISCOVER,
    CATALOG,
    CHATBOT
}

enum class CollectionSubTab(val dbStatus: String) {
    HAVE("Frasco"),
    DECANT("Decant"),
    WISH("Wishlist")
}

enum class CatalogSubTab {
    PERFUMES,
    NOTES
}

data class UserFragranceProfile(
    val topFamily: String,
    val topNotes: List<String>
)

data class RecommendationItem(
    val perfume: PerfumeEntity,
    val matchScore: Int,
    val reasons: List<String>
)

class PerfumeViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = PerfumeRepository(application)

    val myPerfumes: StateFlow<List<PerfumeEntity>> = repository.myPerfumes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sotdHistory: StateFlow<List<SotdEntity>> = repository.sotdHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userProfile: StateFlow<UserProfileEntity?> = repository.userProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _isInitialized = MutableStateFlow(false)
    val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()

    // Navigation states
    private val _currentTab = MutableStateFlow(MainTab.COLLECTION)
    val currentTab: StateFlow<MainTab> = _currentTab.asStateFlow()

    private val _collectionSubTab = MutableStateFlow(CollectionSubTab.HAVE)
    val collectionSubTab: StateFlow<CollectionSubTab> = _collectionSubTab.asStateFlow()

    private val _catalogSubTab = MutableStateFlow(CatalogSubTab.PERFUMES)
    val catalogSubTab: StateFlow<CatalogSubTab> = _catalogSubTab.asStateFlow()

    // Search and filters
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedTagFilter = MutableStateFlow<String?>(null)
    val selectedTagFilter: StateFlow<String?> = _selectedTagFilter.asStateFlow()

    private val _selectedFamilyFilter = MutableStateFlow<String?>(null)
    val selectedFamilyFilter: StateFlow<String?> = _selectedFamilyFilter.asStateFlow()

    // Selected Perfume for modal
    private val _selectedPerfume = MutableStateFlow<PerfumeEntity?>(null)
    val selectedPerfume: StateFlow<PerfumeEntity?> = _selectedPerfume.asStateFlow()

    // Add / Edit Modal
    private val _isAddEditOpen = MutableStateFlow(false)
    val isAddEditOpen: StateFlow<Boolean> = _isAddEditOpen.asStateFlow()

    private val _perfumeToEdit = MutableStateFlow<PerfumeEntity?>(null)
    val perfumeToEdit: StateFlow<PerfumeEntity?> = _perfumeToEdit.asStateFlow()

    // Profile & SOTD Modal
    private val _isProfileModalOpen = MutableStateFlow(false)
    val isProfileModalOpen: StateFlow<Boolean> = _isProfileModalOpen.asStateFlow()

    // Firebase Auth & Cloud Sync
    val currentUser = repository.firebaseManager.currentUser
    val syncStatus = repository.firebaseManager.syncStatus
    val isAdmin: Boolean get() = repository.firebaseManager.isAdmin

    private val _isAuthDialogOpen = MutableStateFlow(false)
    val isAuthDialogOpen: StateFlow<Boolean> = _isAuthDialogOpen.asStateFlow()

    private val _chatHistory = MutableStateFlow<List<com.aistudio.perfumatico.data.remote.Content>>(emptyList())
    val chatHistory: StateFlow<List<com.aistudio.perfumatico.data.remote.Content>> = _chatHistory.asStateFlow()

    private val _isChatLoading = MutableStateFlow(false)
    val isChatLoading: StateFlow<Boolean> = _isChatLoading.asStateFlow()

    private val updateManager = com.aistudio.perfumatico.updater.UpdateManager(application)
    
    private val _updateInfo = MutableStateFlow<com.aistudio.perfumatico.updater.UpdateInfo?>(null)
    val updateInfo: StateFlow<com.aistudio.perfumatico.updater.UpdateInfo?> = _updateInfo.asStateFlow()

    private val _isCheckingUpdate = MutableStateFlow(false)
    val isCheckingUpdate: StateFlow<Boolean> = _isCheckingUpdate.asStateFlow()

    private val _noUpdateAvailable = MutableStateFlow(false)
    val noUpdateAvailable: StateFlow<Boolean> = _noUpdateAvailable.asStateFlow()

    fun checkForUpdates() {
        viewModelScope.launch {
            val info = updateManager.checkForUpdate()
            if (info != null && info.hasUpdate) {
                _updateInfo.value = info
            }
        }
    }

    fun checkUpdatesManually() {
        viewModelScope.launch {
            _isCheckingUpdate.value = true
            val info = updateManager.checkForUpdate()
            _isCheckingUpdate.value = false
            
            if (info != null && info.hasUpdate) {
                _updateInfo.value = info
            } else {
                _noUpdateAvailable.value = true
            }
        }
    }

    fun dismissNoUpdateDialog() {
        _noUpdateAvailable.value = false
    }

    fun downloadUpdate(apkUrl: String) {
        updateManager.downloadAndInstallUpdate(apkUrl)
        _updateInfo.value = null // hide dialog after start
    }
    
    fun dismissUpdate() {
        _updateInfo.value = null
    }

    fun sendMessage(text: String) {
        val userMsg = com.aistudio.perfumatico.data.remote.Content(
            parts = listOf(com.aistudio.perfumatico.data.remote.Part(text = text)),
            role = "user"
        )
        _chatHistory.value = _chatHistory.value + userMsg
        _isChatLoading.value = true

        val assistantMsgIndex = _chatHistory.value.size
        _chatHistory.value = _chatHistory.value + com.aistudio.perfumatico.data.remote.Content(
            parts = listOf(com.aistudio.perfumatico.data.remote.Part(text = "")),
            role = "model"
        )

        viewModelScope.launch {
            val historyToSend = _chatHistory.value.take(assistantMsgIndex)
            val geminiService = com.aistudio.perfumatico.data.remote.GeminiService
            geminiService.chatWithSommelier(historyToSend) { token ->
                _isChatLoading.value = false
                val currentList = _chatHistory.value.toMutableList()
                val currentText = currentList[assistantMsgIndex].parts.firstOrNull()?.text ?: ""
                currentList[assistantMsgIndex] = com.aistudio.perfumatico.data.remote.Content(
                    parts = listOf(com.aistudio.perfumatico.data.remote.Part(text = currentText + token)),
                    role = "model"
                )
                _chatHistory.value = currentList
            }
        }
    }

    // Discover filters
    private val _discoverFamily = MutableStateFlow<String?>(null)
    val discoverFamily: StateFlow<String?> = _discoverFamily.asStateFlow()

    private val _discoverNotes = MutableStateFlow<List<String>>(emptyList())
    val discoverNotes: StateFlow<List<String>> = _discoverNotes.asStateFlow()

    private val _searchNotes = MutableStateFlow<List<String>>(emptyList())
    val searchNotes: StateFlow<List<String>> = _searchNotes.asStateFlow()

    fun toggleSearchNote(note: String) {
        val current = _searchNotes.value
        _searchNotes.value = if (current.contains(note)) {
            current - note
        } else {
            current + note
        }
    }
    
    fun clearSearchNotes() {
        _searchNotes.value = emptyList()
    }

    fun getSimilarPerfumes(perfume: PerfumeEntity): List<PerfumeEntity> {
        val myNotes = perfume.notes.split("|").map { it.trim().lowercase() }.filter { it.isNotBlank() }.toSet()
        if (myNotes.isEmpty()) return emptyList()

        return repository.globalPerfumes
            .filter { it.id != perfume.id }
            .map { other ->
                val otherNotes = other.notes.split("|").map { it.trim().lowercase() }.filter { it.isNotBlank() }.toSet()
                val intersection = myNotes.intersect(otherNotes)
                other to intersection.size
            }
            .filter { it.second > 0 }
            .sortedByDescending { it.second }
            .map { it.first }
            .take(5)
    }

    val globalPerfumes: List<PerfumeEntity> get() = repository.globalPerfumes
    val olfactoryNotes: List<OlfactoryNote> get() = repository.olfactoryNotes

    private val _noteImages = MutableStateFlow<Map<String, String>>(emptyMap())
    val noteImages: StateFlow<Map<String, String>> = _noteImages.asStateFlow()

    init {
        viewModelScope.launch {
            repository.initializeCatalog()
            _isInitialized.value = true
            checkForUpdates()
        }
        viewModelScope.launch {
            repository.noteImages.collect { list ->
                _noteImages.value = list.associate { it.noteName to it.imageUrl }
            }
        }
    }

    fun saveNoteImage(noteName: String, imageUrl: String) {
        viewModelScope.launch {
            repository.saveNoteImage(noteName, imageUrl)
        }
    }

    fun setTab(tab: MainTab) {
        _currentTab.value = tab
    }

    fun setCollectionSubTab(subTab: CollectionSubTab) {
        _collectionSubTab.value = subTab
    }

    fun setCatalogSubTab(subTab: CatalogSubTab) {
        _catalogSubTab.value = subTab
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleTagFilter(tag: String) {
        _selectedTagFilter.value = if (_selectedTagFilter.value == tag) null else tag
    }

    fun setFamilyFilter(family: String?) {
        _selectedFamilyFilter.value = family
    }

    fun openPerfumeDetails(perfume: PerfumeEntity) {
        _selectedPerfume.value = perfume
    }

    fun closePerfumeDetails() {
        _selectedPerfume.value = null
    }

    fun completeNotesWithAI(perfume: PerfumeEntity, onResult: (String) -> Unit) {
        viewModelScope.launch {
            val result = GeminiService.completeOlfactoryPyramid(perfume.name, perfume.brand)
            
            if (result.contains("|")) {
                // Parse and update
                val parts = result.split("|")
                val top = if (parts.isNotEmpty()) parts[0].replace("Saída:", "").trim() else ""
                val heart = if (parts.size > 1) parts[1].replace("Coração:", "").trim() else ""
                val base = if (parts.size > 2) parts[2].replace("Fundo:", "").trim() else ""
                
                val updatedPerfume = perfume.copy(
                    topNotes = top,
                    heartNotes = heart,
                    baseNotes = base,
                    notes = "$top | $heart | $base"
                )
                
                // Keep the modal open with new data if it was open
                if (_selectedPerfume.value?.id == perfume.id) {
                    _selectedPerfume.value = updatedPerfume
                }
                
                // If it's in collection, save to DB
                if (perfume.id.startsWith("my_")) {
                    savePerfume(updatedPerfume)
                }
            }
            
            withContext(Dispatchers.Main) {
                onResult(result)
            }
        }
    }

    fun openAddEdit(perfume: PerfumeEntity? = null) {
        _perfumeToEdit.value = perfume
        _isAddEditOpen.value = true
    }

    
    fun autoFillPerfume(perfumeName: String, onResult: (String) -> Unit) {
        viewModelScope.launch {
            val result = com.aistudio.perfumatico.data.remote.GeminiService.autoFillPerfume(perfumeName)
            withContext(Dispatchers.Main) {
                onResult(result)
            }
        }
    }

    fun closeAddEdit() {
        _perfumeToEdit.value = null
        _isAddEditOpen.value = false
    }

    fun openProfileModal() {
        _isProfileModalOpen.value = true
    }

    fun closeProfileModal() {
        _isProfileModalOpen.value = false
    }

    fun openAuthDialog() {
        _isAuthDialogOpen.value = true
    }

    fun closeAuthDialog() {
        _isAuthDialogOpen.value = false
    }

    fun signInWithEmail(email: String, pass: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                val user = repository.firebaseManager.signInWithEmail(email, pass)
                if (user != null) {
                    saveUserProfile(
                        name = user.displayName ?: email.substringBefore("@"),
                        bio = "Colecionador Perfumático Conectado",
                        signature = ""
                    )
                    closeAuthDialog()
                    onResult(true, null)
                } else {
                    onResult(false, "Falha na autenticação.")
                }
            } catch (e: Exception) {
                onResult(false, e.localizedMessage ?: "Erro ao autenticar.")
            }
        }
    }

    fun signInWithGoogleIdToken(idToken: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                val user = repository.firebaseManager.signInWithGoogleIdToken(idToken)
                if (user != null) {
                    saveUserProfile(
                        name = user.displayName ?: (user.email?.substringBefore("@") ?: "Colecionador"),
                        bio = "Colecionador Perfumático Conectado",
                        signature = ""
                    )
                    closeAuthDialog()
                    onResult(true, null)
                } else {
                    onResult(false, "Falha no login com Google.")
                }
            } catch (e: Exception) {
                onResult(false, e.localizedMessage ?: "Erro no Google Sign-In.")
            }
        }
    }

    fun signOut() {
        repository.firebaseManager.signOut()
        // Limpa o perfil local ao desconectar para evitar vazar o nome para outro usuário
        saveUserProfile(
            name = "Colecionador",
            bio = "Apaixonado por alta perfumaria",
            signature = ""
        )
    }

    fun savePerfume(perfume: PerfumeEntity) {
        viewModelScope.launch {
            repository.savePerfume(perfume)
            closeAddEdit()
            if (_selectedPerfume.value?.id == perfume.id) {
                _selectedPerfume.value = perfume
            }
        }
    }

    fun deletePerfume(perfume: PerfumeEntity) {
        viewModelScope.launch {
            repository.deletePerfume(perfume)
            closePerfumeDetails()
        }
    }

    fun quickAddFromCatalog(catalogPerfume: PerfumeEntity, targetStatus: String) {
        viewModelScope.launch {
            repository.addFromCatalog(catalogPerfume, targetStatus)
        }
    }

    fun registerSotd(perfume: PerfumeEntity, comment: String = "") {
        viewModelScope.launch {
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val dateStr = dateFormat.format(Date())
            repository.addSotd(
                SotdEntity(
                    perfumeId = perfume.id,
                    perfumeName = perfume.name,
                    perfumeBrand = perfume.brand,
                    perfumeImage = perfume.imageUrl,
                    date = dateStr,
                    comment = comment
                )
            )
        }
    }

    fun deleteSotd(id: Long) {
        viewModelScope.launch {
            repository.deleteSotd(id)
        }
    }

    fun saveUserProfile(name: String, bio: String, signature: String) {
        viewModelScope.launch {
            repository.saveProfile(
                UserProfileEntity(
                    id = 1,
                    displayName = name,
                    bio = bio,
                    signaturePerfumeName = signature
                )
            )
        }
    }

    // Recommendation engine
    fun getUserProfileAnalysis(myList: List<PerfumeEntity>): UserFragranceProfile? {
        val collection = myList.filter { it.status == "Já possuo" || it.status == "Pipeline" }
        if (collection.isEmpty()) return null

        val familyCounts = mutableMapOf<String, Int>()
        val noteCounts = mutableMapOf<String, Int>()

        for (p in collection) {
            val fam = p.family.ifBlank { "Fresco" }
            familyCounts[fam] = (familyCounts[fam] ?: 0) + 1

            val rawNotes = p.notes
                .replace("SAÍDA:", "", ignoreCase = true)
                .replace("CORAÇÃO:", "", ignoreCase = true)
                .replace("FUNDO:", "", ignoreCase = true)
                .replace("|", ",")
                .split(",")
                .map { it.trim().uppercase() }
                .filter { it.isNotBlank() }

            rawNotes.forEach { n ->
                noteCounts[n] = (noteCounts[n] ?: 0) + 1
            }
        }

        val topFam = familyCounts.maxByOrNull { it.value }?.key ?: "Fresco"
        val topN = noteCounts.entries
            .sortedByDescending { it.value }
            .take(4)
            .map { it.key }

        return UserFragranceProfile(topFam, topN)
    }

    fun setDiscoverFamily(family: String?) {
        _discoverFamily.value = family
    }

    fun toggleDiscoverNote(note: String) {
        val current = _discoverNotes.value.toMutableList()
        if (current.contains(note)) {
            current.remove(note)
        } else {
            current.add(note)
        }
        _discoverNotes.value = current
    }

    fun clearDiscoverFilters() {
        _discoverFamily.value = null
        _discoverNotes.value = emptyList()
    }
}
