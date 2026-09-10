package com.aistudio.perfumatico.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PerfumeDao {
    @Query("SELECT * FROM perfumes ORDER BY createdAt DESC")
    fun getAllPerfumes(): Flow<List<PerfumeEntity>>

    @Query("SELECT * FROM perfumes WHERE status = :status ORDER BY name ASC")
    fun getPerfumesByStatus(status: String): Flow<List<PerfumeEntity>>

    @Query("SELECT * FROM perfumes WHERE id = :id LIMIT 1")
    suspend fun getPerfumeById(id: String): PerfumeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(perfume: PerfumeEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(perfumes: List<PerfumeEntity>)

    @Delete
    suspend fun delete(perfume: PerfumeEntity)

    @Query("DELETE FROM perfumes WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT COUNT(*) FROM perfumes")
    suspend fun getCount(): Int
}

@Dao
interface SotdDao {
    @Query("SELECT * FROM sotd_history ORDER BY timestamp DESC")
    fun getAllSotd(): Flow<List<SotdEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(sotd: SotdEntity)

    @Delete
    suspend fun delete(sotd: SotdEntity)

    @Query("DELETE FROM sotd_history WHERE id = :id")
    suspend fun deleteById(id: Long)
}

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    fun getProfile(): Flow<UserProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveProfile(profile: UserProfileEntity)
}

@Dao
interface NoteImageDao {
    @Query("SELECT * FROM note_images")
    fun getAllNoteImages(): Flow<List<NoteImageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveNoteImage(noteImage: NoteImageEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(noteImages: List<NoteImageEntity>)
}
