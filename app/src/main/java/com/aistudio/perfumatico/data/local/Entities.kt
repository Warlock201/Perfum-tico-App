package com.aistudio.perfumatico.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "perfumes")
data class PerfumeEntity(
    @PrimaryKey val id: String,
    val name: String,
    val brand: String,
    val family: String = "Fresco",
    val imageUrl: String = "",
    val priceMin: Double = 0.0,
    val priceMax: Double = 0.0,
    val notes: String = "",
    val topNotes: String = "",
    val heartNotes: String = "",
    val baseNotes: String = "",
    val referenceName: String = "",
    val status: String = "Já possuo", // "Já possuo", "Pipeline", "Desejos"
    val tags: String = "", // Comma separated tags
    val fixation: Int = 0, // 0 to 10
    val projection: Int = 0, // 0 to 10
    val personalNotes: String = "",
    val bottlesJson: String = "", // JSON list of bottles
    val macerationStart: String = "",
    val externalRating: Double = 0.0,
    val isCustom: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "sotd_history")
data class SotdEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val perfumeId: String,
    val perfumeName: String,
    val perfumeBrand: String,
    val perfumeImage: String = "",
    val date: String,
    val timestamp: Long = System.currentTimeMillis(),
    val comment: String = ""
)

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: Int = 1,
    val displayName: String = "Colecionador",
    val bio: String = "Apaixonado por alta perfumaria",
    val signaturePerfumeId: String = "",
    val signaturePerfumeName: String = ""
)
