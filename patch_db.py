import re

with open("app/src/main/java/com/aistudio/perfumatico/data/local/AppDatabase.kt", "r") as f:
    content = f.read()

# Add Migration import
if "import androidx.room.migration.Migration" not in content:
    content = content.replace("import androidx.room.RoomDatabase", "import androidx.room.RoomDatabase\nimport androidx.room.migration.Migration\nimport androidx.sqlite.db.SupportSQLiteDatabase")

# Version 4
content = content.replace("version = 3", "version = 4")

migration_code = """
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE perfumes ADD COLUMN markedNewAt INTEGER NOT NULL DEFAULT 0")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
"""
content = content.replace("        fun getDatabase(context: Context): AppDatabase {", migration_code)

builder_code = """
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "perfumatico_db"
                ).addMigrations(MIGRATION_3_4).fallbackToDestructiveMigration().build()
"""
old_builder = """
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "perfumatico_db"
                ).fallbackToDestructiveMigration().build()
"""
content = content.replace(old_builder.strip(), builder_code.strip())

with open("app/src/main/java/com/aistudio/perfumatico/data/local/AppDatabase.kt", "w") as f:
    f.write(content)
