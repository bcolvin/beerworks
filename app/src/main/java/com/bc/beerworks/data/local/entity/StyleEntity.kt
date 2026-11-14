package com.bc.beerworks.data.local.entity

import com.bc.beerworks.data.model.BeerStyle

interface StyleEntity {
    val id: String
    fun toDomainModel(isFavorite: Boolean = false): BeerStyle
}

internal fun String.normalizeIpa(): String =
    this.replace(Regex("\\bipa\\b", RegexOption.IGNORE_CASE), "IPA")
