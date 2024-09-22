package com.eva4
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "place_table")
data class Place(
    @PrimaryKey(autoGenerate = true) var key : Int = 0,
    var id: String = UUID.randomUUID().toString(),
    var name: String = "",
    var imageRef: String = "",
    var latLong: String = "",
    var cost: Double = 0.0,
    var costTrans: Double = 0.0,
    var comment: String = ""
)
