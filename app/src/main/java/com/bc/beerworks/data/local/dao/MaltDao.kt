package com.bc.beerworks.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.bc.beerworks.data.local.entity.MaltVarietyEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MaltDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(malts: List<MaltVarietyEntity>)

    @Query("SELECT * FROM malts ORDER BY name ASC")
    fun getAllMalts(): Flow<List<MaltVarietyEntity>>

    @Query("SELECT * FROM malts WHERE id = :id LIMIT 1")
    fun getMaltById(id: String): Flow<MaltVarietyEntity?>

    @Query("SELECT * FROM malts WHERE id = :id LIMIT 1")
    suspend fun getMaltByIdDirect(id: String): MaltVarietyEntity?

    @Query("SELECT DISTINCT category FROM malts ORDER BY category ASC")
    fun getAllCategories(): Flow<List<String>>

    @Query("SELECT DISTINCT grainType FROM malts ORDER BY grainType ASC")
    fun getAllGrainTypes(): Flow<List<String>>

    @Query(
        """
        SELECT * FROM malts
        WHERE (:ftsQuery = '' OR rowid IN (SELECT rowid FROM malts_fts WHERE malts_fts MATCH :ftsQuery))
          AND (:category IS NULL OR category = :category)
          AND (:grainType IS NULL OR grainType = :grainType)
          AND (:diastaticTier IS NULL OR 
              (:diastaticTier = 'High' AND diastaticPowerLintner >= 100) OR
              (:diastaticTier = 'Moderate' AND diastaticPowerLintner >= 30 AND diastaticPowerLintner < 100) OR
              (:diastaticTier = 'None' AND diastaticPowerLintner < 30))
          AND (:colorTier IS NULL OR
              (:colorTier = 'Pale' AND colorMin < 10.0) OR
              (:colorTier = 'Amber' AND colorMax >= 10.0 AND colorMin <= 50.0) OR
              (:colorTier = 'Brown' AND colorMax >= 50.0 AND colorMin <= 150.0) OR
              (:colorTier = 'Dark' AND colorMax >= 150.0))
          AND (:baseCategory IS NULL OR id IN (
              SELECT maltId FROM malt_style_cross_ref 
              JOIN bjcp_styles ON malt_style_cross_ref.styleId = bjcp_styles.id 
              WHERE bjcp_styles.category = :baseCategory
          ))
        ORDER BY name ASC
        """
    )
    fun searchAndFilterMalts(
        ftsQuery: String,
        category: String?,
        grainType: String?,
        diastaticTier: String?,
        colorTier: String?,
        baseCategory: String?
    ): Flow<List<MaltVarietyEntity>>

    @Query(
        """
        SELECT * FROM malts WHERE id IN (
            SELECT maltId FROM malt_style_cross_ref 
            WHERE styleId = :styleId AND guidelineType = :guidelineType
        ) ORDER BY name ASC
        """
    )
    fun getMaltsForStyle(styleId: String, guidelineType: String): Flow<List<MaltVarietyEntity>>
}
