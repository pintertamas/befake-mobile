package com.pintertamas.befake.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.pintertamas.befake.database.model.ImageCache

@Dao
interface ImageCacheDao {
    @Query("SELECT * FROM imageCache")
    fun getCache(): List<ImageCache>

    @Query("SELECT uri FROM imageCache WHERE tag = :tag LIMIT 1")
    fun get(tag: String): String?

    @Query("INSERT INTO imageCache VALUES(:tag, :uri)")
    fun put(tag: String, uri: String)

    @Query("UPDATE imageCache SET tag = :tag, uri = :uri WHERE tag = :tag")
    fun update(tag: String, uri: String)

    @Query("DELETE FROM imageCache WHERE tag = :tag")
    fun delete(tag: String)

    @Query("DELETE FROM imageCache")
    fun clear()
}