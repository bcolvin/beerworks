package com.bc.beerworks.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.bc.beerworks.data.local.entity.BaStyleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BaStyleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(styles: List<BaStyleEntity>)

    @Query("SELECT * FROM ba_styles ORDER BY CAST(categoryNumber AS INTEGER), number")
    fun getAllStyles(): Flow<List<BaStyleEntity>>

    @Query("SELECT * FROM ba_styles WHERE id = :id LIMIT 1")
    suspend fun getStyleById(id: String): BaStyleEntity?

    @Query("SELECT DISTINCT category FROM ba_styles ORDER BY CAST(categoryNumber AS INTEGER)")
    fun getCategories(): Flow<List<String>>

    @Query("""
        SELECT * FROM ba_styles
        WHERE (:isSrmActive = 0 OR (srmMax IS NOT NULL AND srmMin IS NOT NULL AND srmMax >= :minSrm AND srmMin <= :maxSrm))
          AND (:isAbvActive = 0 OR (abvMax IS NOT NULL AND abvMin IS NOT NULL AND abvMax >= :minAbv AND abvMin <= :maxAbv))
          AND (:isIbuActive = 0 OR (ibuMax IS NOT NULL AND ibuMin IS NOT NULL AND ibuMax >= :minIbu AND ibuMin <= :maxIbu))
          AND (:category IS NULL OR category = :category)
        ORDER BY CAST(categoryNumber AS INTEGER), number
    """)
    fun filterStyles(
        isSrmActive: Int,
        minSrm: Double,
        maxSrm: Double,
        isAbvActive: Int,
        minAbv: Double,
        maxAbv: Double,
        isIbuActive: Int,
        minIbu: Double,
        maxIbu: Double,
        category: String?
    ): Flow<List<BaStyleEntity>>

    @Query("""
        SELECT * FROM ba_styles
        WHERE (
            (:ftsQuery != '' AND rowid IN (SELECT rowid FROM ba_styles_fts WHERE ba_styles_fts MATCH :ftsQuery))
            OR ba_styles.number LIKE :likeQuery
            OR ba_styles.name LIKE :likeQuery
            OR ba_styles.category LIKE :likeQuery
            OR ba_styles.commercialExamples LIKE :likeQuery
            OR ba_styles.characteristicIngredients LIKE :likeQuery
            OR ba_styles.aroma LIKE :likeQuery
            OR ba_styles.flavor LIKE :likeQuery
            OR ba_styles.comments LIKE :likeQuery
            OR ba_styles.tags LIKE :likeQuery
        )
        AND (:isSrmActive = 0 OR (srmMax IS NOT NULL AND srmMin IS NOT NULL AND srmMax >= :minSrm AND srmMin <= :maxSrm))
        AND (:isAbvActive = 0 OR (abvMax IS NOT NULL AND abvMin IS NOT NULL AND abvMax >= :minAbv AND abvMin <= :maxAbv))
        AND (:isIbuActive = 0 OR (ibuMax IS NOT NULL AND ibuMin IS NOT NULL AND ibuMax >= :minIbu AND ibuMin <= :maxIbu))
        AND (:category IS NULL OR ba_styles.category = :category)
        ORDER BY (
            (CASE WHEN ba_styles.name LIKE :likeQuery OR ba_styles.number LIKE :likeQuery THEN 10 ELSE 0 END) +
            (CASE WHEN ba_styles.overallImpression LIKE :likeQuery THEN 5 ELSE 0 END) +
            (CASE WHEN ba_styles.commercialExamples LIKE :likeQuery THEN 5 ELSE 0 END) +
            (CASE WHEN (
                ba_styles.aroma LIKE :likeQuery OR
                ba_styles.flavor LIKE :likeQuery OR
                ba_styles.comments LIKE :likeQuery OR
                ba_styles.characteristicIngredients LIKE :likeQuery OR
                ba_styles.tags LIKE :likeQuery OR
                ba_styles.category LIKE :likeQuery
            ) THEN 1 ELSE 0 END)
        ) DESC, CAST(ba_styles.categoryNumber AS INTEGER), ba_styles.number
    """)
    fun searchAndFilterStyles(
        ftsQuery: String,
        likeQuery: String,
        isSrmActive: Int,
        minSrm: Double,
        maxSrm: Double,
        isAbvActive: Int,
        minAbv: Double,
        maxAbv: Double,
        isIbuActive: Int,
        minIbu: Double,
        maxIbu: Double,
        category: String?
    ): Flow<List<BaStyleEntity>>
}
