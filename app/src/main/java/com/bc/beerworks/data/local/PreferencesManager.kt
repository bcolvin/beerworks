package com.bc.beerworks.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.bc.beerworks.data.model.GuidelineType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "beerworks_prefs")

interface PreferencesManager {
    val selectedGuideline: Flow<GuidelineType>
    suspend fun setGuideline(guidelineType: GuidelineType)
}

class DefaultPreferencesManager(private val context: Context) : PreferencesManager {
    private val GUIDELINE_KEY = stringPreferencesKey("selected_guideline")

    override val selectedGuideline: Flow<GuidelineType> = context.dataStore.data.map { prefs ->
        val name = prefs[GUIDELINE_KEY] ?: GuidelineType.BJCP_2021.name
        runCatching { GuidelineType.valueOf(name) }.getOrDefault(GuidelineType.BJCP_2021)
    }

    override suspend fun setGuideline(guidelineType: GuidelineType) {
        context.dataStore.edit { prefs ->
            prefs[GUIDELINE_KEY] = guidelineType.name
        }
    }
}
