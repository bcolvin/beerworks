package com.bc.beerworks.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.bc.beerworks.data.local.entity.PomeVarietyEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PomeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(pomes: List<PomeVarietyEntity>)

    @Query("SELECT * FROM pomes ORDER BY name ASC")
    fun getAllPomes(): Flow<List<PomeVarietyEntity>>

    @Query("SELECT * FROM pomes WHERE id = :id LIMIT 1")
    fun getPomeById(id: String): Flow<PomeVarietyEntity?>

    @Query("SELECT * FROM pomes WHERE id = :id LIMIT 1")
    suspend fun getPomeByIdDirect(id: String): PomeVarietyEntity?

    @Query("SELECT DISTINCT fruitType FROM pomes ORDER BY fruitType ASC")
    fun getAllFruitTypes(): Flow<List<String>>

    @Query("SELECT DISTINCT classification FROM pomes ORDER BY classification ASC")
    fun getAllClassifications(): Flow<List<String>>

    @Query(
        """
        SELECT * FROM pomes
        WHERE (:ftsQuery = '' OR rowid IN (SELECT rowid FROM pomes_fts WHERE pomes_fts MATCH :ftsQuery))
          AND (:fruitType IS NULL OR fruitType = :fruitType)
          AND (:classification IS NULL OR classification = :classification)
          AND (:harvestSeason IS NULL OR harvestSeason = :harvestSeason)
          AND (:tanninLevel IS NULL OR tanninLevel = :tanninLevel)
          AND (:styleId IS NULL OR id IN (
              SELECT pomeId FROM pome_style_cross_ref 
              WHERE styleId = :styleId
          ))
        ORDER BY name ASC
        """
    )
    fun searchAndFilterPomes(
        ftsQuery: String,
        fruitType: String?,
        classification: String?,
        harvestSeason: String?,
        tanninLevel: String?,
        styleId: String?
    ): Flow<List<PomeVarietyEntity>>

    @Query(
        """
        SELECT * FROM pomes WHERE id IN (
            SELECT pomeId FROM pome_style_cross_ref 
            WHERE styleId = :styleId AND guidelineType = :guidelineType
        ) ORDER BY name ASC
        """
    )
    fun getPomesForStyle(styleId: String, guidelineType: String): Flow<List<PomeVarietyEntity>>

    @Query(
        """
        SELECT * FROM pomes WHERE id IN (
            SELECT pomeId FROM pome_style_cross_ref 
            WHERE styleId IN (:styleIds) AND guidelineType = :guidelineType
        ) ORDER BY name ASC
        """
    )
    fun getPomesForStyles(styleIds: List<String>, guidelineType: String): Flow<List<PomeVarietyEntity>>

    @Query("SELECT COUNT(*) FROM pomes")
    suspend fun getPomeCount(): Int
}
