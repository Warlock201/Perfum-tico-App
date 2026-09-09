package com.aistudio.perfumatico.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.aistudio.perfumatico.data.local.PerfumeEntity
import com.aistudio.perfumatico.data.local.SotdEntity
import com.aistudio.perfumatico.data.local.UserProfileEntity
import com.aistudio.perfumatico.data.model.OlfactoryNote
import com.aistudio.perfumatico.data.repository.PerfumeRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

enum class MainTab {
    COLLECTION,
    DASHBOARD,
    DISCOVER,
    CATALOG
}

enum class CollectionSubTab(val dbStatus: String) {
    HAVE("Já possuo"),
    TO_BUY("Pipeline"),
    WISH("Desejos")
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

    // Discover filters
    private val _discoverFamily = MutableStateFlow<String?>(null)
    val discoverFamily: StateFlow<String?> = _discoverFamily.asStateFlow()

    private val _discoverNotes = MutableStateFlow<List<String>>(emptyList())
    val discoverNotes: StateFlow<List<String>> = _discoverNotes.asStateFlow()

    val globalPerfumes: List<PerfumeEntity> get() = repository.globalPerfumes
    val olfactoryNotes: List<OlfactoryNote> get() = repository.olfactoryNotes

    init {
        viewModelScope.launch {
            repository.initializeCatalog()
            _isInitialized.value = true
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

    fun openAddEdit(perfume: PerfumeEntity? = null) {
        _perfumeToEdit.value = perfume
        _isAddEditOpen.value = true
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
