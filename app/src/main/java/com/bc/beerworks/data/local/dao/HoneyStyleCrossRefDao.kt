package com.bc.beerworks.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.bc.beerworks.data.local.entity.HoneyStyleCrossRef
import kotlinx.coroutines.flow.Flow

@Dao
interface HoneyStyleCrossRefDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(crossRefs: List<HoneyStyleCrossRef>)

    @Query(
        """
        SELECT styleId FROM honey_style_cross_ref
        WHERE honeyId = :honeyId AND guidelineType = :guidelineType
        """
    )
    fun getStyleIdsForHoney(honeyId: String, guidelineType: String): Flow<List<String>>

    @Query(
        """
        SELECT honeyId FROM honey_style_cross_ref
        WHERE styleId = :styleId AND guidelineType = :guidelineType
        """
    )
    fun getHoneyIdsForStyle(styleId: String, guidelineType: String): Flow<List<String>>
}
