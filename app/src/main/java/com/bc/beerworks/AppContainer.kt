package com.bc.beerworks

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.bc.beerworks.data.local.BeerworksDatabase
import com.bc.beerworks.data.local.DefaultPreferencesManager
import com.bc.beerworks.data.repository.BeerStyleRepository
import com.bc.beerworks.data.repository.HoneyRepository
import com.bc.beerworks.data.repository.HopRepository
import com.bc.beerworks.data.repository.IngredientCatalogRepository
import com.bc.beerworks.data.repository.MaltRepository
import com.bc.beerworks.data.repository.OffFlavorRepository
import com.bc.beerworks.data.repository.PomeRepository
import com.bc.beerworks.data.repository.ScoresheetRepository
import com.bc.beerworks.data.repository.YeastRepository

class AppContainer(context: Context) {
    val database: BeerworksDatabase = BeerworksDatabase.getInstance(context)
    val prefsManager: DefaultPreferencesManager = DefaultPreferencesManager(context)
    val beerStyleRepository: BeerStyleRepository = BeerStyleRepository(database, prefsManager)
    val hopRepository: HopRepository = HopRepository(database)
    val maltRepository: MaltRepository = MaltRepository(database)
    val honeyRepository: HoneyRepository = HoneyRepository(database)
    val pomeRepository: PomeRepository = PomeRepository(database)
    val yeastRepository: YeastRepository = YeastRepository(database)
    val offFlavorRepository: OffFlavorRepository = OffFlavorRepository(database)
    val scoresheetRepository: ScoresheetRepository = ScoresheetRepository(database.scoresheetDao())
    val ingredientCatalogRepository: IngredientCatalogRepository = IngredientCatalogRepository(
        hopRepository = hopRepository,
        maltRepository = maltRepository,
        honeyRepository = honeyRepository,
        pomeRepository = pomeRepository,
        yeastRepository = yeastRepository,
        offFlavorRepository = offFlavorRepository
    )
}

inline fun <reified VM : ViewModel> viewModelFactory(
    crossinline create: () -> VM
): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = create() as T
}
