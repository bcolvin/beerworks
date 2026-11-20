package com.bc.beerworks.data.repository

import com.bc.beerworks.data.model.BeerStyle
import com.bc.beerworks.data.model.GuidelineType
import kotlinx.coroutines.flow.Flow

/**
 * Narrow seam for resolving [BeerStyle] records across [GuidelineType]s with live favorite state
 * and canonical ordering.
 */
fun interface StyleLookup {
    fun getStylesByIds(styleIdsByGuideline: Map<GuidelineType, Set<String>>): Flow<List<BeerStyle>>
}
