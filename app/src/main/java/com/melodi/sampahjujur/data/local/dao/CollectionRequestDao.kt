package com.melodi.sampahjujur.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.melodi.sampahjujur.data.local.entity.CollectionRequestEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Collection Requests (SIH 26229 - Person 4).
 */
@Dao
interface CollectionRequestDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(request: CollectionRequestEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(requests: List<CollectionRequestEntity>)

    @Update
    suspend fun update(request: CollectionRequestEntity)

    @Query("SELECT * FROM collection_requests WHERE id = :id")
    suspend fun getById(id: String): CollectionRequestEntity?

    @Query("SELECT * FROM collection_requests WHERE lotId = :lotId")
    suspend fun getByLotId(lotId: String): CollectionRequestEntity?

    @Query("SELECT * FROM collection_requests WHERE lotId = :lotId")
    fun observeByLotId(lotId: String): Flow<CollectionRequestEntity?>

    @Query("SELECT * FROM collection_requests WHERE collectorId = :collectorId ORDER BY createdAt DESC")
    fun observeByCollector(collectorId: String): Flow<List<CollectionRequestEntity>>

    @Query("SELECT * FROM collection_requests WHERE recyclerId = :recyclerId ORDER BY createdAt DESC")
    fun observeByRecycler(recyclerId: String): Flow<List<CollectionRequestEntity>>

    @Query("SELECT * FROM collection_requests ORDER BY createdAt DESC")
    fun observeAllRequests(): Flow<List<CollectionRequestEntity>>

    @Query("SELECT * FROM collection_requests WHERE collectorId = :collectorId ORDER BY createdAt DESC")
    suspend fun getAllByCollector(collectorId: String): List<CollectionRequestEntity>

    @Query("SELECT * FROM collection_requests WHERE recyclerId = :recyclerId ORDER BY createdAt DESC")
    suspend fun getAllByRecycler(recyclerId: String): List<CollectionRequestEntity>

    @Query("SELECT * FROM collection_requests WHERE isSynced = 0 ORDER BY createdAt DESC")
    suspend fun getUnsynced(): List<CollectionRequestEntity>

    @Query("UPDATE collection_requests SET isSynced = 1 WHERE lotId = :lotId")
    suspend fun markAsSynced(lotId: String)

    @Query("DELETE FROM collection_requests WHERE lotId = :lotId")
    suspend fun deleteByLotId(lotId: String): Int

    @Query("SELECT SUM(finalSaleValue) FROM collection_requests WHERE collectorId = :collectorId AND paymentStatus = 'PAID'")
    suspend fun getTotalCompletedEarnings(collectorId: String): Double?

    @Query("SELECT SUM(quotedPrice) FROM collection_requests WHERE collectorId = :collectorId AND paymentStatus = 'PENDING'")
    suspend fun getTotalPendingEarnings(collectorId: String): Double?

    @Query("SELECT COUNT(*) FROM collection_requests WHERE collectorId = :collectorId AND status = 'COMPLETED'")
    suspend fun getCompletedCount(collectorId: String): Int
}
