package com.bc.beerworks.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.bc.beerworks.data.local.entity.HoneyVarietyEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HoneyDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(honeys: List<HoneyVarietyEntity>)

    @Query("SELECT * FROM honeys ORDER BY name ASC")
    fun getAllHoneys(): Flow<List<HoneyVarietyEntity>>

    @Query("SELECT * FROM honeys WHERE id = :id LIMIT 1")
    fun getHoneyById(id: String): Flow<HoneyVarietyEntity?>

    @Query("SELECT * FROM honeys WHERE id = :id LIMIT 1")
    suspend fun getHoneyByIdDirect(id: String): HoneyVarietyEntity?

    @Query("SELECT DISTINCT floralClassification FROM honeys ORDER BY floralClassification ASC")
    fun getAllClassifications(): Flow<List<String>>

    @Query("SELECT DISTINCT usdaColor FROM honeys ORDER BY usdaColor ASC")
    fun getAllUsdaColors(): Flow<List<String>>

    @Query(
        """
        SELECT * FROM honeys
        WHERE (:ftsQuery = '' OR rowid IN (SELECT rowid FROM honeys_fts WHERE honeys_fts MATCH :ftsQuery))
          AND (:classification IS NULL OR floralClassification = :classification)
          AND (:flavorIntensity IS NULL OR flavorIntensity = :flavorIntensity)
          AND (:crystallizationTendency IS NULL OR crystallizationTendency = :crystallizationTendency)
          AND (:styleId IS NULL OR id IN (
              SELECT honeyId FROM honey_style_cross_ref 
              WHERE styleId = :styleId
          ))
        ORDER BY name ASC
        """
    )
    fun searchAndFilterHoneys(
        ftsQuery: String,
        classification: String?,
        flavorIntensity: String?,
        crystallizationTendency: String?,
        styleId: String?
    ): Flow<List<HoneyVarietyEntity>>

    @Query(
        """
        SELECT * FROM honeys WHERE id IN (
            SELECT honeyId FROM honey_style_cross_ref 
            WHERE styleId = :styleId AND guidelineType = :guidelineType
        ) ORDER BY name ASC
        """
    )
    fun getHoneysForStyle(styleId: String, guidelineType: String): Flow<List<HoneyVarietyEntity>>

    @Query(
        """
        SELECT * FROM honeys WHERE id IN (
            SELECT honeyId FROM honey_style_cross_ref 
            WHERE styleId IN (:styleIds) AND guidelineType = :guidelineType
        ) ORDER BY name ASC
        """
    )
    fun getHoneysForStyles(styleIds: List<String>, guidelineType: String): Flow<List<HoneyVarietyEntity>>

    @Query("SELECT COUNT(*) FROM honeys")
    suspend fun getHoneyCount(): Int
}
