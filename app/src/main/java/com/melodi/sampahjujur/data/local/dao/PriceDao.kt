package com.melodi.sampahjujur.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.melodi.sampahjujur.data.local.entity.PriceEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Room database price board and history table.
 */
@Dao
interface PriceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrices(prices: List<PriceEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrice(price: PriceEntity)

    @Query("SELECT * FROM prices WHERE isCurrent = 1")
    fun getAllCurrentPrices(): Flow<List<PriceEntity>>

    @Query("SELECT * FROM prices WHERE isCurrent = 1")
    suspend fun getAllCurrentPricesDirect(): List<PriceEntity>

    @Query("SELECT * FROM prices WHERE material = :material AND isCurrent = 1 LIMIT 1")
    suspend fun getCurrentPriceForMaterial(material: String): PriceEntity?

    @Query("SELECT * FROM prices WHERE material = :material ORDER BY timestamp ASC")
    fun getPriceHistoryForMaterial(material: String): Flow<List<PriceEntity>>

    @Query("SELECT * FROM prices WHERE material = :material ORDER BY timestamp ASC")
    suspend fun getPriceHistoryForMaterialDirect(material: String): List<PriceEntity>

    @Query("DELETE FROM prices")
    suspend fun clearAllPrices()
}
