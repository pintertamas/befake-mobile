package com.pintertamas.befake.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.jetbrains.annotations.NotNull

@Entity(tableName = "imageCache")
data class ImageCache(
    @PrimaryKey
    @NotNull
    @ColumnInfo(name = "tag")
    val tag: String,

    @NotNull
    @ColumnInfo(name = "uri")
    val uri: String
)