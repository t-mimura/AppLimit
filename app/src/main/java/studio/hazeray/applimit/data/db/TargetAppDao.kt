package studio.hazeray.applimit.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TargetAppDao {
    @Query("SELECT * FROM target_apps")
    fun getAll(): Flow<List<TargetAppEntity>>

    @Query("SELECT * FROM target_apps WHERE id = :id")
    suspend fun getById(id: Long): TargetAppEntity?

    @Query("SELECT * FROM target_apps WHERE packageName = :packageName")
    suspend fun getByPackageName(packageName: String): TargetAppEntity?

    @Insert
    suspend fun insert(entity: TargetAppEntity): Long

    @Update
    suspend fun update(entity: TargetAppEntity)

    @Delete
    suspend fun delete(entity: TargetAppEntity)

    @Query("SELECT * FROM target_apps WHERE isEnabled = 1")
    fun getEnabledApps(): Flow<List<TargetAppEntity>>
}
