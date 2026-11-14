package com.bc.beerworks.data.model

import androidx.compose.runtime.Immutable

@Immutable
interface SensoryProfile {
    val defaultStrokeColorHex: Long?
        get() = null

    fun hasData(): Boolean
    fun toAxisMap(): List<Pair<String, Float>>
}
