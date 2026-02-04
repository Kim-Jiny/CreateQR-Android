package com.jiny.createqr.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [QRItemEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun qrItemDao(): QRItemDao
}
