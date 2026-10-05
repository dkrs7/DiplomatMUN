package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [UserEntity::class, DebateSessionEntity::class],
    version = 1,
    exportSchema = false
)
abstract class DiplomatDatabase : RoomDatabase() {
    abstract fun diplomatDao(): DiplomatDao

    companion object {
        @Volatile
        private var INSTANCE: DiplomatDatabase? = null

        fun getDatabase(context: Context): DiplomatDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DiplomatDatabase::class.java,
                    "diplomat_ai_database"
                ).fallbackToDestructiveMigration(true).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
