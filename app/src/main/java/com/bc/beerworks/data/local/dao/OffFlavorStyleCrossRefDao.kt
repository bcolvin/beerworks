package com.bc.beerworks.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.bc.beerworks.data.local.entity.OffFlavorStyleCrossRef
import kotlinx.coroutines.flow.Flow

@Dao
interface OffFlavorStyleCrossRefDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(crossRefs: List<OffFlavorStyleCrossRef>)

    @Query(
        """
        SELECT styleId FROM off_flavor_style_cross_ref
        WHERE offFlavorId = :offFlavorId AND guidelineType = :guidelineType
        """
    )
    fun getStyleIdsForOffFlavor(offFlavorId: String, guidelineType: String): Flow<List<String>>
}
