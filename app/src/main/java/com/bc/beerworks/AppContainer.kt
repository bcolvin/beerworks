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
    private val favoriteIngredientDao = database.favoriteIngredientDao()
    val hopRepository: HopRepository = HopRepository(
        hopDao = database.hopDao(),
        hopStyleCrossRefDao = database.hopStyleCrossRefDao(),
        styleLookup = beerStyleRepository,
        favoriteIngredientDao = favoriteIngredientDao
    )
    val maltRepository: MaltRepository = MaltRepository(
        maltDao = database.maltDao(),
        maltStyleCrossRefDao = database.maltStyleCrossRefDao(),
        styleLookup = beerStyleRepository,
        favoriteIngredientDao = favoriteIngredientDao
    )
    val honeyRepository: HoneyRepository = HoneyRepository(
        honeyDao = database.honeyDao(),
        honeyStyleCrossRefDao = database.honeyStyleCrossRefDao(),
        styleLookup = beerStyleRepository,
        favoriteIngredientDao = favoriteIngredientDao
    )
    val pomeRepository: PomeRepository = PomeRepository(
        pomeDao = database.pomeDao(),
        pomeStyleCrossRefDao = database.pomeStyleCrossRefDao(),
        styleLookup = beerStyleRepository,
        favoriteIngredientDao = favoriteIngredientDao
    )
    val yeastRepository: YeastRepository = YeastRepository(
        yeastDao = database.yeastDao(),
        yeastStyleCrossRefDao = database.yeastStyleCrossRefDao(),
        styleLookup = beerStyleRepository,
        favoriteIngredientDao = favoriteIngredientDao
    )
    val offFlavorRepository: OffFlavorRepository = OffFlavorRepository(
        offFlavorDao = database.offFlavorDao(),
        offFlavorStyleCrossRefDao = database.offFlavorStyleCrossRefDao(),
        styleLookup = beerStyleRepository,
        favoriteIngredientDao = favoriteIngredientDao
    )
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
