package com.bc.beerworks.data.model

import androidx.compose.runtime.Immutable

@Immutable
data class CommercialYeastEquivalent(
    val laboratory: String,
    val productCode: String,
    val productName: String,
    val form: String
)
