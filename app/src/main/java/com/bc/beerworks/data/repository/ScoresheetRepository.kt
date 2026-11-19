package com.bc.beerworks.data.repository

import com.bc.beerworks.data.local.PreferencesManager
import com.bc.beerworks.data.local.dao.ScoresheetDao
import com.bc.beerworks.data.local.entity.toEntity
import com.bc.beerworks.data.model.Scoresheet
import com.bc.beerworks.data.model.ScoresheetDiscipline
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class ScoresheetRepository(
    private val scoresheetDao: ScoresheetDao,
    private val preferencesManager: PreferencesManager? = null,
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default
) {

    val lastScoresheetDiscipline: Flow<ScoresheetDiscipline> =
        preferencesManager?.lastScoresheetDiscipline ?: flowOf(ScoresheetDiscipline.BJCP_BEER)

    suspend fun setLastScoresheetDiscipline(discipline: ScoresheetDiscipline) {
        preferencesManager?.setLastScoresheetDiscipline(discipline)
    }

    fun getAllScoresheets(): Flow<List<Scoresheet>> =
        scoresheetDao.getAllScoresheets()
            .map { entities -> entities.map { it.toDomainModel() } }
            .flowOn(defaultDispatcher)

    suspend fun getScoresheetById(id: String): Scoresheet? = withContext(defaultDispatcher) {
        scoresheetDao.getScoresheetById(id)?.toDomainModel()
    }

    suspend fun saveScoresheet(scoresheet: Scoresheet) {
        val now = System.currentTimeMillis()
        scoresheetDao.upsert(scoresheet.toEntity().copy(updatedAtMillis = now))
    }

    suspend fun deleteScoresheet(id: String) {
        scoresheetDao.deleteById(id)
    }
}
