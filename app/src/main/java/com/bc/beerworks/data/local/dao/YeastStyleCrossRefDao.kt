package com.bc.beerworks.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.bc.beerworks.data.local.entity.YeastStyleCrossRef
import kotlinx.coroutines.flow.Flow

@Dao
interface YeastStyleCrossRefDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(crossRefs: List<YeastStyleCrossRef>)

    @Query(
        """
        SELECT styleId FROM yeast_style_cross_ref
        WHERE yeastId = :yeastId AND guidelineType = :guidelineType
        """
    )
    fun getStyleIdsForYeast(yeastId: String, guidelineType: String): Flow<List<String>>

    @Query(
        """
        SELECT yeastId FROM yeast_style_cross_ref
        WHERE styleId = :styleId AND guidelineType = :guidelineType
        """
    )
    fun getYeastIdsForStyle(styleId: String, guidelineType: String): Flow<List<String>>
}
