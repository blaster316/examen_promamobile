package com.eva4

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "photos_saved_table")
data class PhotosSaved(
    @PrimaryKey var id: String = UUID.randomUUID().toString(),
    var idPlace: String = "",
    var imgUriString: String = ""
)