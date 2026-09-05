package com.melodi.sampahjujur.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.melodi.sampahjujur.data.local.converter.CollectionConverters
import com.melodi.sampahjujur.data.local.converter.TransactionConverters
import com.melodi.sampahjujur.data.local.dao.CollectionRequestDao
import com.melodi.sampahjujur.data.local.dao.HandoverRecordDao
import com.melodi.sampahjujur.data.local.dao.PickupRequestDao
import com.melodi.sampahjujur.data.local.dao.TransactionDao
import com.melodi.sampahjujur.data.local.dao.UserDao
import com.melodi.sampahjujur.data.local.dao.WasteItemDao
import com.melodi.sampahjujur.data.local.entity.CollectionRequestEntity
import com.melodi.sampahjujur.data.local.entity.HandoverRecordEntity
import com.melodi.sampahjujur.data.local.entity.PickupRequestEntity
import com.melodi.sampahjujur.data.local.entity.TransactionEntity
import com.melodi.sampahjujur.data.local.entity.UserEntity
import com.melodi.sampahjujur.data.local.entity.WasteItemEntity

/**
 * Room Database for Sampah Jujur application.
 * Provides local caching and offline-first capabilities for:
 * - Draft waste items (offline creation)
 * - User profiles (faster startup)
 * - Transaction history (offline viewing, complex queries)
 * - Pickup requests (offline submission)
 * - Person 4: Collection requests & digital handover records
 *
 * Database version: 3
 */
@Database(
    entities = [
        WasteItemEntity::class,
        UserEntity::class,
        TransactionEntity::class,
        PickupRequestEntity::class,
        CollectionRequestEntity::class,
        HandoverRecordEntity::class
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(TransactionConverters::class, CollectionConverters::class)
abstract class SampahJujurDatabase : RoomDatabase() {

    /**
     * Provides access to WasteItem operations
     */
    abstract fun wasteItemDao(): WasteItemDao

    /**
     * Provides access to User operations
     */
    abstract fun userDao(): UserDao

    /**
     * Provides access to Transaction operations
     */
    abstract fun transactionDao(): TransactionDao

    /**
     * Provides access to PickupRequest operations
     */
    abstract fun pickupRequestDao(): PickupRequestDao

    /**
     * Person 4: Provides access to CollectionRequest operations
     */
    abstract fun collectionRequestDao(): CollectionRequestDao

    /**
     * Person 4: Provides access to HandoverRecord operations
     */
    abstract fun handoverRecordDao(): HandoverRecordDao

    companion object {
        /**
         * Database name
         */
        const val DATABASE_NAME = "sampah_jujur_db"
    }
}
