package com.wifiprint.app.data.db

import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.wifiprint.app.data.models.PrintJob
import com.wifiprint.app.data.models.ServerInfo
import kotlinx.coroutines.flow.Flow

@Dao
interface PrintJobDao {
    @Query("SELECT * FROM print_jobs ORDER BY createdAt DESC")
    fun getAllJobs(): Flow<List<PrintJob>>

    @Query("SELECT * FROM print_jobs WHERE status = :status ORDER BY createdAt DESC")
    fun getJobsByStatus(status: String): Flow<List<PrintJob>>

    @Query("SELECT * FROM print_jobs WHERE id = :id")
    suspend fun getJobById(id: String): PrintJob?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJob(job: PrintJob)

    @Update
    suspend fun updateJob(job: PrintJob)

    @Delete
    suspend fun deleteJob(job: PrintJob)

    @Query("DELETE FROM print_jobs WHERE completedAt IS NOT NULL AND completedAt < :beforeTimestamp")
    suspend fun deleteOldJobs(beforeTimestamp: Long)
}

@Dao
interface ServerDao {
    @Query("SELECT * FROM servers ORDER BY lastConnected DESC")
    fun getAllServers(): Flow<List<ServerInfo>>

    @Query("SELECT * FROM servers WHERE isPaired = 1 ORDER BY lastConnected DESC LIMIT 1")
    suspend fun getLastPairedServer(): ServerInfo?

    @Query("SELECT * FROM servers WHERE id = :id")
    suspend fun getServerById(id: String): ServerInfo?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertServer(server: ServerInfo)

    @Update
    suspend fun updateServer(server: ServerInfo)

    @Delete
    suspend fun deleteServer(server: ServerInfo)
}

@Database(
    entities = [PrintJob::class, ServerInfo::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun printJobDao(): PrintJobDao
    abstract fun serverDao(): ServerDao

    companion object {
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE servers ADD COLUMN certificateFingerprint TEXT")
                db.execSQL("ALTER TABLE servers ADD COLUMN lastAuthCheckAt INTEGER")
                db.execSQL("ALTER TABLE servers ADD COLUMN connectionHealth TEXT NOT NULL DEFAULT 'Unknown'")
            }
        }
    }
}
