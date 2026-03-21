package com.utfpr.posmoveis.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.utfpr.posmoveis.database.model.UserLocation

@Dao
interface UserLocationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(userLocation: UserLocation)


    @Query("SELECT * FROM user_location_table")
    suspend fun getAllUserLocation(): List<UserLocation>

    @Query("SELECT * FROM user_location_table order by id desc limit 1")
    suspend fun getLastLocation(): UserLocation?
}