package com.bc.beerworks.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.bc.beerworks.data.local.entity.YeastStrainEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface YeastDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(yeasts: List<YeastStrainEntity>)

    @Query("SELECT * FROM yeasts ORDER BY name ASC")
    fun getAllYeasts(): Flow<List<YeastStrainEntity>>

    @Query("SELECT * FROM yeasts WHERE id = :id LIMIT 1")
    fun getYeastById(id: String): Flow<YeastStrainEntity?>

    @Query("SELECT * FROM yeasts WHERE id = :id LIMIT 1")
    suspend fun getYeastByIdDirect(id: String): YeastStrainEntity?

    @Query("SELECT DISTINCT classification FROM yeasts ORDER BY classification ASC")
    fun getAllClassifications(): Flow<List<String>>

    @Query("SELECT DISTINCT flocculation FROM yeasts ORDER BY flocculation ASC")
    fun getAllFlocculations(): Flow<List<String>>

    @Query(
        """
        SELECT * FROM yeasts
        WHERE (:ftsQuery = '' OR rowid IN (SELECT rowid FROM yeasts_fts WHERE yeasts_fts MATCH :ftsQuery))
          AND (:classification IS NULL OR classification = :classification)
          AND (:flocculation IS NULL OR flocculation = :flocculation)
          AND (:minAttenuation IS NULL OR attenuationMax >= :minAttenuation)
          AND (:minAlcoholTolerance IS NULL OR alcoholTolerance >= :minAlcoholTolerance)
          AND (:styleId IS NULL OR id IN (
              SELECT yeastId FROM yeast_style_cross_ref 
              WHERE styleId = :styleId
          ))
        ORDER BY name ASC
        """
    )
    fun searchAndFilterYeasts(
        ftsQuery: String,
        classification: String?,
        flocculation: String?,
        minAttenuation: Double?,
        minAlcoholTolerance: Double?,
        styleId: String?
    ): Flow<List<YeastStrainEntity>>

    @Query(
        """
        SELECT * FROM yeasts WHERE id IN (
            SELECT yeastId FROM yeast_style_cross_ref 
            WHERE styleId = :styleId AND guidelineType = :guidelineType
        ) ORDER BY name ASC
        """
    )
    fun getYeastsForStyle(styleId: String, guidelineType: String): Flow<List<YeastStrainEntity>>

    @Query(
        """
        SELECT * FROM yeasts WHERE id IN (
            SELECT yeastId FROM yeast_style_cross_ref 
            WHERE styleId IN (:styleIds) AND guidelineType = :guidelineType
        ) ORDER BY name ASC
        """
    )
    fun getYeastsForStyles(styleIds: List<String>, guidelineType: String): Flow<List<YeastStrainEntity>>

    @Query("SELECT COUNT(*) FROM yeasts")
    suspend fun getYeastCount(): Int
}
