package com.bc.beerworks.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.bc.beerworks.data.local.entity.PomeStyleCrossRef
import kotlinx.coroutines.flow.Flow

@Dao
interface PomeStyleCrossRefDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(crossRefs: List<PomeStyleCrossRef>)

    @Query(
        """
        SELECT styleId FROM pome_style_cross_ref
        WHERE pomeId = :pomeId AND guidelineType = :guidelineType
        """
    )
    fun getStyleIdsForPome(pomeId: String, guidelineType: String): Flow<List<String>>

    @Query(
        """
        SELECT pomeId FROM pome_style_cross_ref
        WHERE styleId = :styleId AND guidelineType = :guidelineType
        """
    )
    fun getPomeIdsForStyle(styleId: String, guidelineType: String): Flow<List<String>>
}
