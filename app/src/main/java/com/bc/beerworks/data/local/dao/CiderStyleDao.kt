package com.bc.beerworks.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.bc.beerworks.data.local.entity.CiderStyleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CiderStyleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(styles: List<CiderStyleEntity>)

    @Query("SELECT * FROM cider_styles ORDER BY categoryNumber, number")
    fun getAllStyles(): Flow<List<CiderStyleEntity>>

    @Query("SELECT * FROM cider_styles WHERE id = :id LIMIT 1")
    suspend fun getStyleById(id: String): CiderStyleEntity?

    @Query("SELECT DISTINCT category FROM cider_styles ORDER BY categoryNumber")
    fun getCategories(): Flow<List<String>>

    @Query("""
        SELECT * FROM cider_styles
        WHERE (:isAbvActive = 0 OR (abvMax IS NOT NULL AND abvMin IS NOT NULL AND abvMax >= :minAbv AND abvMin <= :maxAbv))
          AND (:category IS NULL OR category = :category)
        ORDER BY categoryNumber, number
    """)
    fun filterStyles(
        isAbvActive: Int,
        minAbv: Double,
        maxAbv: Double,
        category: String?
    ): Flow<List<CiderStyleEntity>>

    @Query("""
        SELECT * FROM cider_styles
        WHERE (
            (:ftsQuery != '' AND rowid IN (SELECT rowid FROM cider_styles_fts WHERE cider_styles_fts MATCH :ftsQuery))
            OR cider_styles.number LIKE :likeQuery
            OR cider_styles.name LIKE :likeQuery
            OR cider_styles.category LIKE :likeQuery
            OR cider_styles.commercialExamples LIKE :likeQuery
            OR cider_styles.characteristicIngredients LIKE :likeQuery
            OR cider_styles.aroma LIKE :likeQuery
            OR cider_styles.flavor LIKE :likeQuery
            OR cider_styles.appearance LIKE :likeQuery
            OR cider_styles.mouthfeel LIKE :likeQuery
            OR cider_styles.comments LIKE :likeQuery
            OR cider_styles.tags LIKE :likeQuery
            OR cider_styles.sweetness LIKE :likeQuery
            OR cider_styles.carbonation LIKE :likeQuery
            OR cider_styles.strength LIKE :likeQuery
        )
        AND (:isAbvActive = 0 OR (abvMax IS NOT NULL AND abvMin IS NOT NULL AND abvMax >= :minAbv AND abvMin <= :maxAbv))
        AND (:category IS NULL OR cider_styles.category = :category)
        ORDER BY (
            (CASE WHEN cider_styles.name LIKE :likeQuery OR cider_styles.number LIKE :likeQuery THEN 10 ELSE 0 END) +
            (CASE WHEN cider_styles.overallImpression LIKE :likeQuery THEN 5 ELSE 0 END) +
            (CASE WHEN cider_styles.commercialExamples LIKE :likeQuery THEN 5 ELSE 0 END) +
            (CASE WHEN (
                cider_styles.aroma LIKE :likeQuery OR
                cider_styles.flavor LIKE :likeQuery OR
                cider_styles.appearance LIKE :likeQuery OR
                cider_styles.mouthfeel LIKE :likeQuery OR
                cider_styles.comments LIKE :likeQuery OR
                cider_styles.characteristicIngredients LIKE :likeQuery OR
                cider_styles.tags LIKE :likeQuery OR
                cider_styles.category LIKE :likeQuery OR
                cider_styles.sweetness LIKE :likeQuery OR
                cider_styles.carbonation LIKE :likeQuery OR
                cider_styles.strength LIKE :likeQuery
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
    ): Flow<List<CiderStyleEntity>>
}
