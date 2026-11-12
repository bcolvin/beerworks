package com.bc.beerworks.data.model

import androidx.compose.runtime.Immutable

@Immutable
interface Ingredient {
    val id: String
    val name: String
    val origin: String
    val countryCode: String
    val description: String
    val descriptors: List<String>
    val substituteIds: List<String>
    val countryFlag: String
}
