package com.bc.beerworks.data.repository

import com.bc.beerworks.data.local.PreferencesManager
import com.bc.beerworks.data.local.dao.ScoresheetDao
import com.bc.beerworks.data.local.entity.toEntity
import com.bc.beerworks.data.model.Scoresheet
import com.bc.beerworks.data.model.ScoresheetFormType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class ScoresheetRepository(
    private val scoresheetDao: ScoresheetDao,
    private val preferencesManager: PreferencesManager
) {
    val preferredFormType: Flow<ScoresheetFormType> = preferencesManager.scoresheetFormType

    suspend fun setPreferredFormType(formType: ScoresheetFormType) {
        preferencesManager.setScoresheetFormType(formType)
    }

    fun getAllScoresheets(): Flow<List<Scoresheet>> =
        scoresheetDao.getAllScoresheets()
            .map { entities -> entities.map { it.toDomainModel() } }
            .flowOn(Dispatchers.Default)

    suspend fun getScoresheetById(id: String): Scoresheet? = withContext(Dispatchers.Default) {
        scoresheetDao.getScoresheetById(id)?.toDomainModel()
    }

    suspend fun saveScoresheet(scoresheet: Scoresheet) {
        scoresheetDao.upsert(
            scoresheet.copy(updatedAtMillis = System.currentTimeMillis()).toEntity()
        )
    }

    suspend fun deleteScoresheet(id: String) {
        scoresheetDao.deleteById(id)
    }
}
