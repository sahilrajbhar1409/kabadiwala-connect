package com.melodi.sampahjujur.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.melodi.sampahjujur.data.local.entity.PriceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PriceDao {
    @Query("SELECT * FROM reference_prices ORDER BY displayName")
    fun observeAll(): Flow<List<PriceEntity>>

    @Query("SELECT * FROM reference_prices WHERE category = :category LIMIT 1")
    suspend fun get(category: String): PriceEntity?

    @Upsert
    suspend fun upsertAll(prices: List<PriceEntity>)
}
