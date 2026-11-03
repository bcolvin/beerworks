package com.bc.beerworks.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.bc.beerworks.data.local.entity.HopStyleCrossRef
import kotlinx.coroutines.flow.Flow

@Dao
interface HopStyleCrossRefDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(crossRefs: List<HopStyleCrossRef>)

    @Query(
        """
        SELECT styleId FROM hop_style_cross_ref
        WHERE hopId = :hopId AND guidelineType = :guidelineType
        """
    )
    fun getStyleIdsForHop(hopId: String, guidelineType: String): Flow<List<String>>

    @Query(
        """
        SELECT hopId FROM hop_style_cross_ref
        WHERE styleId = :styleId AND guidelineType = :guidelineType
        """
    )
    fun getHopIdsForStyle(styleId: String, guidelineType: String): Flow<List<String>>
}
