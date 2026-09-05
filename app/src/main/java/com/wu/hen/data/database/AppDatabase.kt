package com.wu.hen.data.database

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

/**
 * 下载历史记录（用于跨进程重启的任务历史与存储管理）
 */
@Entity(tableName = "download_records")
data class DownloadRecordEntity(
    @PrimaryKey val id: String,
    val url: String,
    val title: String,
    val author: String,
    val platformName: String,
    val thumbnailUrl: String?,
    val filePath: String,
    val fileSizeBytes: Long,
    val status: String,
    val createdAtMillis: Long,
)

@Dao
interface DownloadRecordDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: DownloadRecordEntity)

    @Query("SELECT * FROM download_records ORDER BY createdAtMillis DESC")
    fun getAll(): Flow<List<DownloadRecordEntity>>

    @Query("DELETE FROM download_records WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM download_records")
    suspend fun clear()
}

@Database(entities = [DownloadRecordEntity::class], version = 1, exportSchema = true)
abstract class AppDatabase : RoomDatabase() {

    abstract fun downloadRecordDao(): DownloadRecordDao

    companion object {
        fun build(context: Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, "wuhen.db").build()
    }
}
