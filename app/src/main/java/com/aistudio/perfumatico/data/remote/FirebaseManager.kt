package com.aistudio.perfumatico.data.remote

import android.content.Context
import android.util.Log
import com.aistudio.perfumatico.data.local.PerfumeEntity
import com.aistudio.perfumatico.data.local.SotdEntity
import com.aistudio.perfumatico.data.local.UserProfileEntity
import com.aistudio.perfumatico.utils.cleanNotePrefix
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class FirebaseManager private constructor(private val context: Context) {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val _currentUser = MutableStateFlow<FirebaseUser?>(auth.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    private val _syncStatus = MutableStateFlow("Sincronizado")
    val syncStatus: StateFlow<String> = _syncStatus.asStateFlow()

    private var perfumesListener: ListenerRegistration? = null
    private var sotdListener: ListenerRegistration? = null

    val adminEmail = "wargamesrex5j6@gmail.com"

    val isAdmin: Boolean
        get() = _currentUser.value?.email?.equals(adminEmail, ignoreCase = true) == true

    init {
        auth.addAuthStateListener { firebaseAuth ->
            _currentUser.value = firebaseAuth.currentUser
        }
    }

    fun startListeningToUserData(
        onPerfumesUpdated: (List<PerfumeEntity>) -> Unit,
        onSotdUpdated: (List<SotdEntity>) -> Unit
    ) {
        val user = auth.currentUser ?: return
        val uid = user.uid

        // Stop previous listeners if any
        perfumesListener?.remove()
        sotdListener?.remove()

        _syncStatus.value = "Sincronizando..."

        // Listen to user's personal perfumes: users/{uid}/perfumes
        perfumesListener = firestore.collection("users")
            .document(uid)
            .collection("perfumes")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("FirebaseManager", "Error listening to user perfumes", error)
                    _syncStatus.value = "Erro na sincronização"
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val list = mutableListOf<PerfumeEntity>()
                    for (doc in snapshot.documents) {
                        try {
                            val data = doc.data ?: continue
                            val entity = docToPerfumeEntity(doc.id, data)
                            list.add(entity)
                        } catch (e: Exception) {
                            Log.e("FirebaseManager", "Error parsing perfume doc", e)
                        }
                    }
                    onPerfumesUpdated(list)
                    _syncStatus.value = "Nuvem conectada (${list.size} perfumes)"
                }
            }

        // Listen to SOTD: users/{uid}/sotd
        sotdListener = firestore.collection("users")
            .document(uid)
            .collection("sotd")
            .addSnapshotListener { snapshot, error ->
                if (error == null && snapshot != null) {
                    val sotdList = mutableListOf<SotdEntity>()
                    for (doc in snapshot.documents) {
                        try {
                            val data = doc.data ?: continue
                            val entity = SotdEntity(
                                id = doc.getLong("id") ?: System.currentTimeMillis(),
                                perfumeId = data["perfumeId"] as? String ?: "",
                                perfumeName = data["perfumeName"] as? String ?: "",
                                perfumeBrand = data["perfumeBrand"] as? String ?: "",
                                perfumeImage = data["perfumeImage"] as? String ?: "",
                                date = data["date"] as? String ?: "",
                                timestamp = (data["timestamp"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                                comment = data["comment"] as? String ?: ""
                            )
                            sotdList.add(entity)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    onSotdUpdated(sotdList.sortedByDescending { it.timestamp })
                }
            }
    }

    suspend fun uploadPerfume(perfume: PerfumeEntity) {
        val user = auth.currentUser ?: return
        val uid = user.uid

        val map = hashMapOf(
            "id" to perfume.id,
            "name" to perfume.name,
            "brand" to perfume.brand,
            "family" to perfume.family,
            "imageUrl" to perfume.imageUrl,
            "priceMin" to perfume.priceMin,
            "priceMax" to perfume.priceMax,
            "notes" to perfume.notes,
            "topNotes" to perfume.topNotes,
            "heartNotes" to perfume.heartNotes,
            "baseNotes" to perfume.baseNotes,
            "referenceName" to perfume.referenceName,
            "status" to perfume.status,
            "userPreference" to perfume.userPreference,
            "tags" to perfume.tags,
            "fixation" to perfume.fixation,
            "projection" to perfume.projection,
            "personalNotes" to perfume.personalNotes,
            "bottlesJson" to perfume.bottlesJson,
            "macerationStart" to perfume.macerationStart,
            "isCustom" to perfume.isCustom,
            "updatedAt" to System.currentTimeMillis()
        )

        firestore.collection("users")
            .document(uid)
            .collection("perfumes")
            .document(perfume.id)
            .set(map, SetOptions.merge())
            .await()

        // If admin and item is in global catalogue or marked custom, can also update global_perfumes
        if (isAdmin && !perfume.isCustom) {
            firestore.collection("global_perfumes")
                .document(perfume.id)
                .set(map, SetOptions.merge())
        }
    }

    suspend fun deletePerfume(perfumeId: String) {
        val user = auth.currentUser ?: return
        val uid = user.uid

        firestore.collection("users")
            .document(uid)
            .collection("perfumes")
            .document(perfumeId)
            .delete()
            .await()
    }

    suspend fun uploadSotd(sotd: SotdEntity) {
        val user = auth.currentUser ?: return
        val uid = user.uid

        val docId = if (sotd.id != 0L) sotd.id.toString() else System.currentTimeMillis().toString()
        val map = hashMapOf(
            "id" to (if (sotd.id != 0L) sotd.id else System.currentTimeMillis()),
            "perfumeId" to sotd.perfumeId,
            "perfumeName" to sotd.perfumeName,
            "perfumeBrand" to sotd.perfumeBrand,
            "perfumeImage" to sotd.perfumeImage,
            "date" to sotd.date,
            "timestamp" to sotd.timestamp,
            "comment" to sotd.comment
        )

        firestore.collection("users")
            .document(uid)
            .collection("sotd")
            .document(docId)
            .set(map, SetOptions.merge())
            .await()
    }

    suspend fun deleteSotd(id: Long) {
        val user = auth.currentUser ?: return
        val uid = user.uid

        firestore.collection("users")
            .document(uid)
            .collection("sotd")
            .document(id.toString())
            .delete()
            .await()
    }

    suspend fun uploadProfile(profile: UserProfileEntity) {
        val user = auth.currentUser ?: return
        val uid = user.uid

        val map = hashMapOf(
            "displayName" to profile.displayName,
            "bio" to profile.bio,
            "signaturePerfumeName" to profile.signaturePerfumeName,
            "email" to (user.email ?: "")
        )

        firestore.collection("users")
            .document(uid)
            .collection("profile")
            .document("info")
            .set(map, SetOptions.merge())
            .await()
    }

    suspend fun signInWithGoogleIdToken(idToken: String): FirebaseUser? {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val result = auth.signInWithCredential(credential).await()
        return result.user
    }

    suspend fun signInWithEmail(email: String, pass: String): FirebaseUser? {
        return try {
            val result = auth.signInWithEmailAndPassword(email, pass).await()
            result.user
        } catch (e: Exception) {
            // Try creating if doesn't exist
            val createResult = auth.createUserWithEmailAndPassword(email, pass).await()
            createResult.user
        }
    }

    fun signOut() {
        perfumesListener?.remove()
        sotdListener?.remove()
        auth.signOut()
        _currentUser.value = null
        _syncStatus.value = "Offline (Local)"
    }

    private fun extractStringOrList(data: Map<String, Any>, vararg keys: String): String {
        for (key in keys) {
            val value = data[key]
            if (value != null && value.toString().isNotBlank()) {
                return if (value is List<*>) {
                    value.joinToString(", ")
                } else {
                    value.toString()
                }
            }
        }
        return ""
    }

    private fun docToPerfumeEntity(id: String, data: Map<String, Any>): PerfumeEntity {
        val name = extractStringOrList(data, "name", "nome").ifBlank { "Sem nome" }
        val brand = extractStringOrList(data, "brand", "marca").ifBlank { "Marca" }
        val family = extractStringOrList(data, "family", "familia").ifBlank { "Fresco" }
        val imageUrl = extractStringOrList(data, "imageUrl", "imagem")
        val priceMin = (data["priceMin"] as? Number)?.toDouble() ?: 200.0
        val priceMax = (data["priceMax"] as? Number)?.toDouble() ?: 350.0
        val notes = extractStringOrList(data, "notes", "notas")
        
        // Handle nested piramideOlfativa or notas object if it exists
        val piramide = (data["piramideOlfativa"] as? Map<*, *>) ?: (data["notas"] as? Map<*, *>)
        
        var topNotes = piramide?.let { it["topo"]?.toString() } ?: extractStringOrList(data, "topNotes", "notasTopo", "notasSaida")
        var heartNotes = piramide?.let { it["coracao"]?.toString() } ?: extractStringOrList(data, "heartNotes", "notasCoracao", "notasMeio")
        var baseNotes = piramide?.let { it["fundo"]?.toString() } ?: extractStringOrList(data, "baseNotes", "notasFundo", "notasBase")
        
        // Se as notas ainda estiverem vazias e existir a string unificada com "|"
        if (topNotes.isBlank() && heartNotes.isBlank() && baseNotes.isBlank() && notes.contains("|")) {
            val parts = notes.split("|").map { it.trim() }
            if (parts.isNotEmpty()) topNotes = parts[0]
            if (parts.size > 1) heartNotes = parts[1]
            if (parts.size > 2) baseNotes = parts[2]
        }
        
        val ref = extractStringOrList(data, "referenceName", "referencia")
        val status = extractStringOrList(data, "status").ifBlank { "Já possuo" }
        val userPref = (data["userPreference"] as? Number)?.toInt() ?: 0
        val tags = extractStringOrList(data, "tags").ifBlank { "DIA A DIA" }
        val fixation = (data["fixation"] as? Number)?.toInt() ?: (data["longevidade"] as? Number)?.toInt() ?: 7
        val projection = (data["projection"] as? Number)?.toInt() ?: (data["projeção"] as? Number)?.toInt() ?: 7
        val personalNotes = extractStringOrList(data, "personalNotes", "notasPessoais")
        val bottles = extractStringOrList(data, "bottlesJson", "frascos").ifBlank { "[\"100ml\"]" }
        val isCustom = (data["isCustom"] as? Boolean) ?: false

        return PerfumeEntity(
            id = id,
            name = name,
            brand = brand,
            family = family,
            imageUrl = imageUrl,
            priceMin = priceMin,
            priceMax = priceMax,
            notes = notes,
            topNotes = topNotes.cleanNotePrefix(),
            heartNotes = heartNotes.cleanNotePrefix(),
            baseNotes = baseNotes.cleanNotePrefix(),
            referenceName = ref,
            status = status,
            userPreference = userPref,
            tags = tags,
            fixation = fixation,
            projection = projection,
            personalNotes = personalNotes,
            bottlesJson = bottles,
            isCustom = isCustom
        )
    }

    companion object {
        @Volatile
        private var INSTANCE: FirebaseManager? = null

        fun getInstance(context: Context): FirebaseManager {
            return INSTANCE ?: synchronized(this) {
                val instance = FirebaseManager(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
}
