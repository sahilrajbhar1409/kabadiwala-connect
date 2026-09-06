package com.melodi.sampahjujur.data.local.dao

import androidx.room.Dao
<<<<<<< HEAD
import androidx.room.Query
import androidx.room.Upsert
=======
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
>>>>>>> f8f13893033af90e6bddcbdb82ab63c12f831ffd
import com.melodi.sampahjujur.data.local.entity.PriceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PriceDao {
<<<<<<< HEAD
    @Query("SELECT * FROM reference_prices ORDER BY displayName")
    fun observeAll(): Flow<List<PriceEntity>>

    @Query("SELECT * FROM reference_prices WHERE category = :category LIMIT 1")
    suspend fun get(category: String): PriceEntity?

    @Upsert
    suspend fun upsertAll(prices: List<PriceEntity>)
}
=======
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(rows: List<PriceEntity>)

    @Query("SELECT * FROM price_cache ORDER BY category, recordedAt DESC")
    fun observeAll(): Flow<List<PriceEntity>>

    @Query("SELECT * FROM price_cache WHERE category = :category ORDER BY recordedAt ASC")
    fun observeHistory(category: String): Flow<List<PriceEntity>>
}
>>>>>>> f8f13893033af90e6bddcbdb82ab63c12f831ffd
