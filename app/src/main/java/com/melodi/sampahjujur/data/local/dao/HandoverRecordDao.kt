package com.melodi.sampahjujur.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.melodi.sampahjujur.data.local.entity.HandoverRecordEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Handover Records (SIH 26229 - Person 4).
 */
@Dao
interface HandoverRecordDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: HandoverRecordEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(records: List<HandoverRecordEntity>)

    @Update
    suspend fun update(record: HandoverRecordEntity)

    @Query("SELECT * FROM handover_records WHERE handoverId = :handoverId")
    suspend fun getByHandoverId(handoverId: String): HandoverRecordEntity?

    @Query("SELECT * FROM handover_records WHERE handoverReference = :ref")
    suspend fun getByHandoverReference(ref: String): HandoverRecordEntity?

    @Query("SELECT * FROM handover_records WHERE lotId = :lotId")
    suspend fun getByLotId(lotId: String): HandoverRecordEntity?

    @Query("SELECT * FROM handover_records WHERE lotId = :lotId")
    fun observeByLotId(lotId: String): Flow<HandoverRecordEntity?>

    @Query("SELECT * FROM handover_records WHERE collectorId = :collectorId ORDER BY timestamp DESC")
    fun observeByCollector(collectorId: String): Flow<List<HandoverRecordEntity>>

    @Query("SELECT * FROM handover_records WHERE collectorId = :collectorId ORDER BY timestamp DESC")
    suspend fun getAllByCollector(collectorId: String): List<HandoverRecordEntity>

    @Query("SELECT * FROM handover_records WHERE isSynced = 0 ORDER BY timestamp DESC")
    suspend fun getUnsynced(): List<HandoverRecordEntity>

    @Query("UPDATE handover_records SET isSynced = 1 WHERE handoverId = :handoverId")
    suspend fun markAsSynced(handoverId: String)
}
