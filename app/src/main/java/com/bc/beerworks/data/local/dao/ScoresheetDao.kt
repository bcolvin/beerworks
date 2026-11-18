package com.bc.beerworks.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.bc.beerworks.data.local.entity.ScoresheetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ScoresheetDao {
    @Query("SELECT * FROM scoresheets ORDER BY evaluatedAtMillis DESC, updatedAtMillis DESC")
    fun getAllScoresheets(): Flow<List<ScoresheetEntity>>

    @Query("SELECT * FROM scoresheets WHERE id = :id LIMIT 1")
    suspend fun getScoresheetById(id: String): ScoresheetEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(scoresheet: ScoresheetEntity)

    @Query("DELETE FROM scoresheets WHERE id = :id")
    suspend fun deleteById(id: String)
}
