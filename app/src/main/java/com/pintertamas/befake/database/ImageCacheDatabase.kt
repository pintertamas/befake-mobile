package com.pintertamas.befake.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.pintertamas.befake.database.converter.ImageBitmapString
import com.pintertamas.befake.database.dao.ImageCacheDao
import com.pintertamas.befake.database.model.ImageCache

@Database(
    entities = [ImageCache::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(
    ImageBitmapString::class
)
abstract class ImageCacheDatabase : RoomDatabase() {
    abstract fun imageCacheDao(): ImageCacheDao
}