package com.bc.beerworks.data.model

import androidx.compose.runtime.Immutable

@Immutable
data class StyleIngredients(
    val hops: List<HopVariety> = emptyList(),
    val malts: List<MaltVariety> = emptyList(),
    val honeys: List<HoneyVariety> = emptyList(),
    val pomes: List<PomeVariety> = emptyList(),
    val yeasts: List<YeastStrain> = emptyList()
)
