package com.bc.beerworks.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.bc.beerworks.data.local.entity.OffFlavorEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OffFlavorDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(offFlavors: List<OffFlavorEntity>)

    @Query("SELECT * FROM off_flavors ORDER BY name ASC")
    fun getAllOffFlavors(): Flow<List<OffFlavorEntity>>

    @Query("SELECT * FROM off_flavors WHERE id = :id LIMIT 1")
    fun getOffFlavorById(id: String): Flow<OffFlavorEntity?>

    @Query("SELECT * FROM off_flavors WHERE id = :id LIMIT 1")
    suspend fun getOffFlavorByIdDirect(id: String): OffFlavorEntity?

    @Query("SELECT DISTINCT category FROM off_flavors ORDER BY category ASC")
    fun getAllCategories(): Flow<List<String>>

    @Query(
        """
        SELECT * FROM off_flavors
        WHERE (:ftsQuery = '' OR rowid IN (SELECT rowid FROM off_flavors_fts WHERE off_flavors_fts MATCH :ftsQuery))
          AND (:category IS NULL OR category = :category)
          AND (:modality IS NULL OR detectedIn LIKE '%' || :modality || '%')
          AND (:discipline IS NULL OR disciplines LIKE '%' || :discipline || '%')
          AND (:permissibility IS NULL OR stylePermissibility = :permissibility)
        ORDER BY name ASC
        """
    )
    fun searchAndFilterOffFlavors(
        ftsQuery: String,
        category: String?,
        modality: String?,
        discipline: String?,
        permissibility: String?
    ): Flow<List<OffFlavorEntity>>

    @Query("SELECT COUNT(*) FROM off_flavors")
    suspend fun getOffFlavorCount(): Int
}
