package com.melodi.sampahjujur.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.melodi.sampahjujur.data.local.entity.PriceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PriceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(rows: List<PriceEntity>)

    @Query("SELECT * FROM price_cache ORDER BY category, recordedAt DESC")
    fun observeAll(): Flow<List<PriceEntity>>

    @Query("SELECT * FROM price_cache WHERE category = :category ORDER BY recordedAt ASC")
    fun observeHistory(category: String): Flow<List<PriceEntity>>
}