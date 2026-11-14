package com.bc.beerworks.data.local.seeder

import android.content.res.AssetManager
import com.google.gson.Gson

fun interface DomainSeeder {
    suspend fun seedIfNeeded(assets: AssetManager, gson: Gson)
}

object StyleGuidelineResolver {
    fun inferGuideline(styleId: String): String = when {
        styleId.startsWith("C") -> "BJCP_CIDER"
        styleId.startsWith("M") -> "BJCP_MEAD"
        else -> "BJCP_2021"
    }
}
