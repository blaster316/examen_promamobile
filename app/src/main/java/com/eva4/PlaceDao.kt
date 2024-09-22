package com.eva4

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface PlaceDao {
    @Insert
    fun insertPlace(place: Place)

    @Query("SELECT * FROM place_table WHERE id = :idPlace")
    fun getPlaceById(idPlace: String): Place

    @Query("SELECT * FROM place_table")
    fun getAllPlaces(): List<Place>

    @Delete
    fun deletePlace(place: Place)

    @Update
    fun updatePlace(place: Place)
}
