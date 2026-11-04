package com.bc.beerworks.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.bc.beerworks.data.local.entity.HopVarietyEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HopDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(hops: List<HopVarietyEntity>)

    @Query("SELECT * FROM hops ORDER BY name ASC")
    fun getAllHops(): Flow<List<HopVarietyEntity>>

    @Query("SELECT * FROM hops WHERE id = :id LIMIT 1")
    fun getHopById(id: String): Flow<HopVarietyEntity?>

    @Query("SELECT * FROM hops WHERE id = :id LIMIT 1")
    suspend fun getHopByIdDirect(id: String): HopVarietyEntity?

    @Query("SELECT * FROM hops WHERE parentHopIds LIKE '%' || :parentHopId || '%' ORDER BY name ASC")
    fun getChildHops(parentHopId: String): Flow<List<HopVarietyEntity>>

    @Query("SELECT DISTINCT origin FROM hops ORDER BY origin ASC")
    fun getAllOrigins(): Flow<List<String>>

    @Query(
        """
        SELECT * FROM hops
        WHERE (:ftsQuery = '' OR rowid IN (SELECT rowid FROM hops_fts WHERE hops_fts MATCH :ftsQuery))
          AND (:purpose IS NULL OR purpose = :purpose OR purpose = 'Dual-Purpose')
          AND (:sensoryCategory IS NULL OR sensoryCategories LIKE '%' || :sensoryCategory || '%')
          AND (:isAlphaFiltered = 0 OR (alphaAcidMax >= :minAlpha AND alphaAcidMin <= :maxAlpha))
          AND (:originCount = 0 OR origin IN (:origins))
          AND (:baseCategory IS NULL OR id IN (
              SELECT hopId FROM hop_style_cross_ref 
              JOIN bjcp_styles ON hop_style_cross_ref.styleId = bjcp_styles.id 
              WHERE bjcp_styles.category = :baseCategory
          ))
        ORDER BY 
          CASE 
            WHEN :purpose IS NOT NULL AND purpose = :purpose THEN 0 
            WHEN :purpose IS NOT NULL AND purpose = 'Dual-Purpose' THEN 1 
            ELSE 2 
          END ASC,
          name ASC
        """
    )
    fun searchAndFilterHops(
        ftsQuery: String,
        purpose: String?,
        sensoryCategory: String?,
        isAlphaFiltered: Int,
        minAlpha: Double,
        maxAlpha: Double,
        originCount: Int,
        origins: List<String>,
        baseCategory: String?
    ): Flow<List<HopVarietyEntity>>

    @Query(
        """
        SELECT * FROM hops WHERE id IN (
            SELECT hopId FROM hop_style_cross_ref 
            WHERE styleId = :styleId AND guidelineType = :guidelineType
        ) ORDER BY name ASC
        """
    )
    fun getHopsForStyle(styleId: String, guidelineType: String): Flow<List<HopVarietyEntity>>
}
