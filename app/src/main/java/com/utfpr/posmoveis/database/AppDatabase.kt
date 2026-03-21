package com.utfpr.posmoveis.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.utfpr.posmoveis.database.converters.DateConverters
import com.utfpr.posmoveis.database.dao.UserLocationDao
import com.utfpr.posmoveis.database.model.UserLocation

@Database(entities = [UserLocation::class], version = 1, exportSchema = true)
@TypeConverters(DateConverters::class)
abstract class AppDatabase : RoomDatabase()  {

    abstract fun userLocationDao(): UserLocationDao

}