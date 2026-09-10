package com.aistudio.perfumatico.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [PerfumeEntity::class, SotdEntity::class, UserProfileEntity::class, NoteImageEntity::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun perfumeDao(): PerfumeDao
    abstract fun sotdDao(): SotdDao
    abstract fun userProfileDao(): UserProfileDao
    abstract fun noteImageDao(): NoteImageDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "perfumatico_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
