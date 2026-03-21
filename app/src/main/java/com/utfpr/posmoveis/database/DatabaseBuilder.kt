package com.utfpr.posmoveis.database

import android.content.Context
import androidx.room.Room

object DatabaseBuilder {
// singleton pattern
    private var INSTANCE: AppDatabase? = null

    fun getInstance(context: Context? = null): AppDatabase {
        return INSTANCE ?: synchronized(this) {
            if (context == null) {
                throw IllegalStateException("Context cannot be null")
            }

            val instance = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "app_database"
            )
                .build()
            INSTANCE = instance
            instance
        }
    }

}