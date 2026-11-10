package com.bc.beerworks.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.bc.beerworks.data.local.entity.MeadStyleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MeadStyleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(styles: List<MeadStyleEntity>)

    @Query("SELECT * FROM mead_styles ORDER BY categoryNumber, number")
    fun getAllStyles(): Flow<List<MeadStyleEntity>>

    @Query("SELECT * FROM mead_styles WHERE id = :id LIMIT 1")
    suspend fun getStyleById(id: String): MeadStyleEntity?

    @Query("SELECT DISTINCT category FROM mead_styles ORDER BY categoryNumber")
    fun getCategories(): Flow<List<String>>

    @Query("""
        SELECT * FROM mead_styles
        WHERE (:isAbvActive = 0 OR (abvMax IS NOT NULL AND abvMin IS NOT NULL AND abvMax >= :minAbv AND abvMin <= :maxAbv))
          AND (:category IS NULL OR category = :category)
        ORDER BY categoryNumber, number
    """)
    fun filterStyles(
        isAbvActive: Int,
        minAbv: Double,
        maxAbv: Double,
        category: String?
    ): Flow<List<MeadStyleEntity>>

    @Query("""
        SELECT * FROM mead_styles
        WHERE (
            (:ftsQuery != '' AND rowid IN (SELECT rowid FROM mead_styles_fts WHERE mead_styles_fts MATCH :ftsQuery))
            OR mead_styles.number LIKE :likeQuery
            OR mead_styles.name LIKE :likeQuery
            OR mead_styles.category LIKE :likeQuery
            OR mead_styles.commercialExamples LIKE :likeQuery
            OR mead_styles.characteristicIngredients LIKE :likeQuery
            OR mead_styles.aroma LIKE :likeQuery
            OR mead_styles.flavor LIKE :likeQuery
            OR mead_styles.appearance LIKE :likeQuery
            OR mead_styles.mouthfeel LIKE :likeQuery
            OR mead_styles.comments LIKE :likeQuery
            OR mead_styles.tags LIKE :likeQuery
            OR mead_styles.sweetness LIKE :likeQuery
            OR mead_styles.carbonation LIKE :likeQuery
            OR mead_styles.strength LIKE :likeQuery
        )
        AND (:isAbvActive = 0 OR (abvMax IS NOT NULL AND abvMin IS NOT NULL AND abvMax >= :minAbv AND abvMin <= :maxAbv))
        AND (:category IS NULL OR mead_styles.category = :category)
        ORDER BY (
            (CASE WHEN mead_styles.name LIKE :likeQuery OR mead_styles.number LIKE :likeQuery THEN 10 ELSE 0 END) +
            (CASE WHEN mead_styles.overallImpression LIKE :likeQuery THEN 5 ELSE 0 END) +
            (CASE WHEN mead_styles.commercialExamples LIKE :likeQuery THEN 5 ELSE 0 END) +
            (CASE WHEN (
                mead_styles.aroma LIKE :likeQuery OR
                mead_styles.flavor LIKE :likeQuery OR
                mead_styles.appearance LIKE :likeQuery OR
                mead_styles.mouthfeel LIKE :likeQuery OR
                mead_styles.comments LIKE :likeQuery OR
                mead_styles.characteristicIngredients LIKE :likeQuery OR
                mead_styles.tags LIKE :likeQuery OR
                mead_styles.category LIKE :likeQuery OR
                mead_styles.sweetness LIKE :likeQuery OR
                mead_styles.carbonation LIKE :likeQuery OR
                mead_styles.strength LIKE :likeQuery
            ) THEN 1 ELSE 0 END)
        ) DESC, categoryNumber, number
    """)
    fun searchAndFilterStyles(
        ftsQuery: String,
        likeQuery: String,
        isAbvActive: Int,
        minAbv: Double,
        maxAbv: Double,
        category: String?
    ): Flow<List<MeadStyleEntity>>
}
