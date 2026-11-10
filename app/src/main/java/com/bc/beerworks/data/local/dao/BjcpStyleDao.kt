package com.bc.beerworks.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.bc.beerworks.data.local.entity.BjcpStyleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BjcpStyleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(styles: List<BjcpStyleEntity>)

    @Query("SELECT * FROM bjcp_styles ORDER BY CAST(categoryNumber AS INTEGER), number")
    fun getAllStyles(): Flow<List<BjcpStyleEntity>>

    @Query("SELECT * FROM bjcp_styles WHERE id = :id LIMIT 1")
    suspend fun getStyleById(id: String): BjcpStyleEntity?

    @Query("SELECT DISTINCT category FROM bjcp_styles ORDER BY CAST(categoryNumber AS INTEGER)")
    fun getCategories(): Flow<List<String>>

    @Query("""
        SELECT * FROM bjcp_styles
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
    ): Flow<List<BjcpStyleEntity>>

    @Query("""
        SELECT * FROM bjcp_styles
        WHERE (
            (:ftsQuery != '' AND rowid IN (SELECT rowid FROM bjcp_styles_fts WHERE bjcp_styles_fts MATCH :ftsQuery))
            OR bjcp_styles.number LIKE :likeQuery
            OR bjcp_styles.name LIKE :likeQuery
            OR bjcp_styles.category LIKE :likeQuery
            OR bjcp_styles.commercialExamples LIKE :likeQuery
            OR bjcp_styles.characteristicIngredients LIKE :likeQuery
            OR bjcp_styles.aroma LIKE :likeQuery
            OR bjcp_styles.flavor LIKE :likeQuery
            OR bjcp_styles.comments LIKE :likeQuery
            OR bjcp_styles.tags LIKE :likeQuery
        )
        AND (:isSrmActive = 0 OR (srmMax IS NOT NULL AND srmMin IS NOT NULL AND srmMax >= :minSrm AND srmMin <= :maxSrm))
        AND (:isAbvActive = 0 OR (abvMax IS NOT NULL AND abvMin IS NOT NULL AND abvMax >= :minAbv AND abvMin <= :maxAbv))
        AND (:isIbuActive = 0 OR (ibuMax IS NOT NULL AND ibuMin IS NOT NULL AND ibuMax >= :minIbu AND ibuMin <= :maxIbu))
        AND (:category IS NULL OR bjcp_styles.category = :category)
        ORDER BY (
            (CASE WHEN bjcp_styles.name LIKE :likeQuery OR bjcp_styles.number LIKE :likeQuery THEN 10 ELSE 0 END) +
            (CASE WHEN bjcp_styles.overallImpression LIKE :likeQuery THEN 5 ELSE 0 END) +
            (CASE WHEN bjcp_styles.commercialExamples LIKE :likeQuery THEN 5 ELSE 0 END) +
            (CASE WHEN (
                bjcp_styles.aroma LIKE :likeQuery OR
                bjcp_styles.flavor LIKE :likeQuery OR
                bjcp_styles.comments LIKE :likeQuery OR
                bjcp_styles.characteristicIngredients LIKE :likeQuery OR
                bjcp_styles.tags LIKE :likeQuery OR
                bjcp_styles.category LIKE :likeQuery
            ) THEN 1 ELSE 0 END)
        ) DESC, CAST(bjcp_styles.categoryNumber AS INTEGER), bjcp_styles.number
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
    ): Flow<List<BjcpStyleEntity>>
}
