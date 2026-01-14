package com.createqr.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface QRItemDao {
    @Query("SELECT * FROM qr_items ORDER BY createdAt DESC")
    fun getAllQRItems(): Flow<List<QRItemEntity>>

    @Query("SELECT * FROM qr_items WHERE id = :id")
    suspend fun getQRItemById(id: Long): QRItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQRItem(item: QRItemEntity): Long

    @Update
    suspend fun updateQRItem(item: QRItemEntity)

    @Delete
    suspend fun deleteQRItem(item: QRItemEntity)

    @Query("DELETE FROM qr_items WHERE id = :id")
    suspend fun deleteQRItemById(id: Long)
}
