package com.aistudio.perfumatico.data.repository

import com.aistudio.perfumatico.utils.cleanNotePrefix
import com.aistudio.perfumatico.utils.normalizeNoteName
import android.content.Context
import com.aistudio.perfumatico.data.local.AppDatabase
import com.aistudio.perfumatico.data.local.PerfumeEntity
import com.aistudio.perfumatico.data.local.SotdEntity
import com.aistudio.perfumatico.data.local.UserProfileEntity
import com.aistudio.perfumatico.data.model.OlfactoryNote
import com.aistudio.perfumatico.data.remote.FirebaseManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStreamReader
import java.util.UUID

data class RawPerfumeJson(
    val nome: String?,
    val marca: String?,
    val familia: String?,
    val notas: String?,
    val longevidade: Int?,
    val projeção: Int?,
    val priceMin: Double?,
    val priceMax: Double?,
    val imageUrl: String?,
    val referencia: String?
)

data class RawNoteJson(
    val nome: String?,
    val imageUrl: String?
)

class PerfumeRepository(private val context: Context) {
    private val db = AppDatabase.getDatabase(context)
    private val perfumeDao = db.perfumeDao()
    private val sotdDao = db.sotdDao()
    private val userProfileDao = db.userProfileDao()
    private val noteImageDao = db.noteImageDao()

    val noteImages: kotlinx.coroutines.flow.Flow<List<com.aistudio.perfumatico.data.local.NoteImageEntity>> = noteImageDao.getAllNoteImages()

    suspend fun saveNoteImage(noteName: String, imageUrl: String) = withContext(Dispatchers.IO) {
        noteImageDao.saveNoteImage(com.aistudio.perfumatico.data.local.NoteImageEntity(noteName.normalizeNoteName(), imageUrl))
    }

    private val gson = Gson()
    val firebaseManager = FirebaseManager.getInstance(context)

    // In-memory catalog of all 200+ global reference perfumes from dataset
    private val _globalPerfumesList = mutableListOf<PerfumeEntity>()
    private val _globalPerfumes = MutableStateFlow<List<PerfumeEntity>>(emptyList())
    val globalPerfumes: StateFlow<List<PerfumeEntity>> = _globalPerfumes.asStateFlow()

    // In-memory olfactory notes
    private val _olfactoryNotesList = mutableListOf<OlfactoryNote>()
    private val _olfactoryNotes = MutableStateFlow<List<OlfactoryNote>>(emptyList())
    val olfactoryNotes: StateFlow<List<OlfactoryNote>> = _olfactoryNotes.asStateFlow()

    val myPerfumes: Flow<List<PerfumeEntity>> = perfumeDao.getAllPerfumes()
    val sotdHistory: Flow<List<SotdEntity>> = sotdDao.getAllSotd()
    val userProfile: Flow<UserProfileEntity?> = userProfileDao.getProfile()

    init {
        // Automatically listen to Firestore when a user is logged in
        CoroutineScope(Dispatchers.IO).launch {
            firebaseManager.currentUser.collect { user ->
                if (user != null) {
                    firebaseManager.startListeningToUserData(
                        onPerfumesUpdated = { cloudPerfumes ->
                            CoroutineScope(Dispatchers.IO).launch {
                                if (cloudPerfumes.isNotEmpty()) {
                                    perfumeDao.insertAll(cloudPerfumes)
                                }
                            }
                        },
                        onSotdUpdated = { cloudSotd ->
                            CoroutineScope(Dispatchers.IO).launch {
                                for (item in cloudSotd) {
                                    sotdDao.insert(item)
                                }
                            }
                        }
                    )
                }
            }
        }
    }

    suspend fun initializeCatalog() = withContext(Dispatchers.IO) {
        if (_globalPerfumesList.isNotEmpty()) return@withContext

        try {
            // Load base perfumes
            context.assets.open("base_perfumes.json").use { inputStream ->
                InputStreamReader(inputStream).use { reader ->
                    val type = object : TypeToken<List<RawPerfumeJson>>() {}.type
                    val rawList: List<RawPerfumeJson> = gson.fromJson(reader, type) ?: emptyList()
                    val converted = rawList.mapIndexed { index, raw ->
                        val parts = (raw.notas ?: "").split("|")
                        val top = if (parts.isNotEmpty()) parts[0].cleanNotePrefix() else ""
                        val heart = if (parts.size > 1) parts[1].cleanNotePrefix() else ""
                        val base = if (parts.size > 2) parts[2].cleanNotePrefix() else ""

                        PerfumeEntity(
                            id = "catalog_${index}_${(raw.nome ?: "").filter { it.isLetterOrDigit() }}",
                            name = (raw.nome ?: "Sem nome").trim(),
                            brand = (raw.marca ?: "Genérico").trim(),
                            family = (raw.familia ?: "Fresco").trim(),
                            imageUrl = raw.imageUrl?.trim() ?: "",
                            priceMin = raw.priceMin ?: 200.0,
                            priceMax = raw.priceMax ?: 350.0,
                            notes = (raw.notas ?: "").trim(),
                            topNotes = top,
                            heartNotes = heart,
                            baseNotes = base,
                            referenceName = raw.referencia?.trim() ?: "",
                            status = "Já possuo",
                            fixation = raw.longevidade ?: 7,
                            projection = raw.projeção ?: 7,
                            tags = "DIA A DIA, ASSINATURA",
                            bottlesJson = "[\"100ml\"]",
                            isCustom = false
                        )
                    }
                    _globalPerfumesList.addAll(converted)
                    _globalPerfumes.value = _globalPerfumesList.toList()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            // Load olfactory notes
            context.assets.open("base_notes.json").use { inputStream ->
                InputStreamReader(inputStream).use { reader ->
                    val type = object : TypeToken<List<RawNoteJson>>() {}.type
                    val rawNotes: List<RawNoteJson> = gson.fromJson(reader, type) ?: emptyList()
                    val converted = rawNotes.mapIndexed { index, raw ->
                        OlfactoryNote(
                            id = "note_$index",
                            nome = (raw.nome ?: "").uppercase().trim(),
                            imageUrl = raw.imageUrl ?: ""
                        )
                    }.filter { it.nome.isNotBlank() }
                    _olfactoryNotesList.addAll(converted)
                    _olfactoryNotes.value = _olfactoryNotesList.toList()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Seed initial NoteImages if empty
        val currentNotes = noteImageDao.getAllNoteImages().first()
        if (currentNotes.isEmpty()) {
            val entities = com.aistudio.perfumatico.data.local.DefaultNoteImages.map { (name, url) ->
                com.aistudio.perfumatico.data.local.NoteImageEntity(name, url)
            }
            noteImageDao.insertAll(entities)
        }

        // Seed default profile if empty (do NOT seed demo perfumes into user collection)
        val profile = userProfileDao.getProfile().first()
        if (profile == null) {
            userProfileDao.saveProfile(
                UserProfileEntity(
                    id = 1,
                    displayName = "Colecionador Perfumático",
                    bio = "Amante da perfumaria de nicho e designers.",
                    signaturePerfumeName = ""
                )
            )
        }

        // Clean any residual prefixes in local database
        try {
            val existingPerfumes = perfumeDao.getAllPerfumes().first()
            val needCleaning = existingPerfumes.filter { p ->
                p.topNotes.contains(Regex("^(sa[ií]da|topo|top):", RegexOption.IGNORE_CASE)) ||
                p.heartNotes.contains(Regex("^(cora[cç][aã]o|coracao|heart):", RegexOption.IGNORE_CASE)) ||
                p.baseNotes.contains(Regex("^(fundo|base):", RegexOption.IGNORE_CASE)) ||
                p.notes.contains(Regex("(sa[ií]da|cora[cç][aã]o|fundo):", RegexOption.IGNORE_CASE))
            }
            if (needCleaning.isNotEmpty()) {
                val cleaned = needCleaning.map { p ->
                    val cleanTop = p.topNotes.cleanNotePrefix()
                    val cleanHeart = p.heartNotes.cleanNotePrefix()
                    val cleanBase = p.baseNotes.cleanNotePrefix()
                    val cleanNotes = listOf(cleanTop, cleanHeart, cleanBase).filter { it.isNotBlank() }.joinToString(" | ")
                    p.copy(
                        topNotes = cleanTop,
                        heartNotes = cleanHeart,
                        baseNotes = cleanBase,
                        notes = if (cleanNotes.isNotBlank()) cleanNotes else p.notes
                    )
                }
                perfumeDao.insertAll(cleaned)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun savePerfume(perfume: PerfumeEntity) = withContext(Dispatchers.IO) {
        perfumeDao.insertOrUpdate(perfume)
        try {
            firebaseManager.uploadPerfume(perfume)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun deletePerfume(perfume: PerfumeEntity) = withContext(Dispatchers.IO) {
        perfumeDao.delete(perfume)
        try {
            firebaseManager.deletePerfume(perfume.id)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun deletePerfumeById(id: String) = withContext(Dispatchers.IO) {
        perfumeDao.deleteById(id)
        try {
            firebaseManager.deletePerfume(id)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun addSotd(sotd: SotdEntity) = withContext(Dispatchers.IO) {
        sotdDao.insert(sotd)
        try {
            firebaseManager.uploadSotd(sotd)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun deleteSotd(id: Long) = withContext(Dispatchers.IO) {
        sotdDao.deleteById(id)
        try {
            firebaseManager.deleteSotd(id)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun saveProfile(profile: UserProfileEntity) = withContext(Dispatchers.IO) {
        userProfileDao.saveProfile(profile)
        try {
            firebaseManager.uploadProfile(profile)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Quick add from catalog to collection
    suspend fun addFromCatalog(catalogPerfume: PerfumeEntity, targetStatus: String) = withContext(Dispatchers.IO) {
        val userCopy = catalogPerfume.copy(
            id = "my_${UUID.randomUUID()}",
            status = targetStatus,
            createdAt = System.currentTimeMillis()
        )
        perfumeDao.insertOrUpdate(userCopy)
        try {
            firebaseManager.uploadPerfume(userCopy)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
