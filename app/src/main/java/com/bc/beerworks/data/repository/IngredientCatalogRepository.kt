package com.bc.beerworks.data.repository

import com.bc.beerworks.data.local.BeerworksDatabase
import com.bc.beerworks.data.model.GuidelineType
import com.bc.beerworks.data.model.HoneyVariety
import com.bc.beerworks.data.model.HopVariety
import com.bc.beerworks.data.model.MaltVariety
import com.bc.beerworks.data.model.OffFlavor
import com.bc.beerworks.data.model.PomeVariety
import com.bc.beerworks.data.model.StyleIngredients
import com.bc.beerworks.data.model.YeastStrain
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

@OptIn(ExperimentalCoroutinesApi::class)
open class IngredientCatalogRepository(
    val hopRepository: IngredientRepository<HopVariety, *>,
    val maltRepository: IngredientRepository<MaltVariety, *>,
    val honeyRepository: IngredientRepository<HoneyVariety, *>,
    val pomeRepository: IngredientRepository<PomeVariety, *>,
    val yeastRepository: IngredientRepository<YeastStrain, *>,
    val offFlavorRepository: IngredientRepository<OffFlavor, *>? = null,
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default
) {
    constructor(
        database: BeerworksDatabase,
        defaultDispatcher: CoroutineDispatcher = Dispatchers.Default
    ) : this(
        hopRepository = HopRepository(database, defaultDispatcher),
        maltRepository = MaltRepository(database, defaultDispatcher),
        honeyRepository = HoneyRepository(database, defaultDispatcher),
        pomeRepository = PomeRepository(database, defaultDispatcher),
        yeastRepository = YeastRepository(database, defaultDispatcher),
        offFlavorRepository = OffFlavorRepository(database, defaultDispatcher),
        defaultDispatcher = defaultDispatcher
    )

    open fun getIngredientsForStyle(
        styleId: String,
        guidelineType: GuidelineType
    ): Flow<StyleIngredients> {
        return hopRepository.getItemsForStyle(styleId, guidelineType).flatMapLatest { hops ->
            maltRepository.getItemsForStyle(styleId, guidelineType).flatMapLatest { malts ->
                honeyRepository.getItemsForStyle(styleId, guidelineType).flatMapLatest { honeys ->
                    pomeRepository.getItemsForStyle(styleId, guidelineType).flatMapLatest { pomes ->
                        yeastRepository.getItemsForStyle(styleId, guidelineType).map { yeasts ->
                            StyleIngredients(
                                hops = hops,
                                malts = malts,
                                honeys = honeys,
                                pomes = pomes,
                                yeasts = yeasts
                            )
                        }
                    }
                }
            }
        }.flowOn(defaultDispatcher)
    }

    open fun getFavoriteIngredients(): Flow<StyleIngredients> {
        val offFlavorsFlow = offFlavorRepository?.getFavoriteItems() ?: flowOf(emptyList())
        return hopRepository.getFavoriteItems().flatMapLatest { hops ->
            maltRepository.getFavoriteItems().flatMapLatest { malts ->
                honeyRepository.getFavoriteItems().flatMapLatest { honeys ->
                    pomeRepository.getFavoriteItems().flatMapLatest { pomes ->
                        yeastRepository.getFavoriteItems().flatMapLatest { yeasts ->
                            offFlavorsFlow.map { offFlavors ->
                                StyleIngredients(
                                    hops = hops,
                                    malts = malts,
                                    honeys = honeys,
                                    pomes = pomes,
                                    yeasts = yeasts,
                                    offFlavors = offFlavors
                                )
                            }
                        }
                    }
                }
            }
        }.flowOn(defaultDispatcher)
    }

    open suspend fun removeFavoriteIngredient(type: String, id: String) {
        when (type) {
            "HOP" -> hopRepository.toggleFavorite(id, true)
            "MALT" -> maltRepository.toggleFavorite(id, true)
            "HONEY" -> honeyRepository.toggleFavorite(id, true)
            "POME" -> pomeRepository.toggleFavorite(id, true)
            "YEAST" -> yeastRepository.toggleFavorite(id, true)
            OffFlavorRepository.TYPE_OFF_FLAVOR -> offFlavorRepository?.toggleFavorite(id, true)
        }
    }
}
