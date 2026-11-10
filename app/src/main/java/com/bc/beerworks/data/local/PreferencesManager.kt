package com.bc.beerworks.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
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
    val keepScreenOn: Flow<Boolean>
    suspend fun setKeepScreenOn(enabled: Boolean)
}

class DefaultPreferencesManager(private val context: Context) : PreferencesManager {
    private val GUIDELINE_KEY = stringPreferencesKey("selected_guideline")
    private val KEEP_SCREEN_ON_KEY = booleanPreferencesKey("keep_screen_on")

    override val selectedGuideline: Flow<GuidelineType> = context.dataStore.data.map { prefs ->
        val name = prefs[GUIDELINE_KEY] ?: GuidelineType.BJCP_2021.name
        runCatching { GuidelineType.valueOf(name) }.getOrDefault(GuidelineType.BJCP_2021)
    }

    override suspend fun setGuideline(guidelineType: GuidelineType) {
        context.dataStore.edit { prefs ->
            prefs[GUIDELINE_KEY] = guidelineType.name
        }
    }

    override val keepScreenOn: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEEP_SCREEN_ON_KEY] ?: false
    }

    override suspend fun setKeepScreenOn(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEEP_SCREEN_ON_KEY] = enabled
        }
    }
}
