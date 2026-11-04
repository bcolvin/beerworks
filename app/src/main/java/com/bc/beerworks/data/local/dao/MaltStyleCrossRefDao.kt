package com.bc.beerworks.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.bc.beerworks.data.local.entity.MaltStyleCrossRef
import kotlinx.coroutines.flow.Flow

@Dao
interface MaltStyleCrossRefDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(crossRefs: List<MaltStyleCrossRef>)

    @Query(
        """
        SELECT styleId FROM malt_style_cross_ref
        WHERE maltId = :maltId AND guidelineType = :guidelineType
        """
    )
    fun getStyleIdsForMalt(maltId: String, guidelineType: String): Flow<List<String>>

    @Query(
        """
        SELECT maltId FROM malt_style_cross_ref
        WHERE styleId = :styleId AND guidelineType = :guidelineType
        """
    )
    fun getMaltIdsForStyle(styleId: String, guidelineType: String): Flow<List<String>>
}
