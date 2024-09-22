package com.eva4

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface PhotoSavedDao {
    @Query("SELECT * FROM photos_saved_table WHERE idPlace = :idPlace")
     fun getPhotosByPlaceId(idPlace: String): List<PhotosSaved>

    @Insert
     fun insertPhoto(photo: PhotosSaved)
}