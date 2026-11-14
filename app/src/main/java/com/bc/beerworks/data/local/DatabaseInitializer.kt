package com.bc.beerworks.data.local

import android.content.Context
import com.bc.beerworks.data.local.seeder.DomainSeeder
import com.bc.beerworks.data.local.seeder.HoneySeeder
import com.bc.beerworks.data.local.seeder.HopSeeder
import com.bc.beerworks.data.local.seeder.MaltSeeder
import com.bc.beerworks.data.local.seeder.PomeSeeder
import com.bc.beerworks.data.local.seeder.StyleSeeder
import com.bc.beerworks.data.local.seeder.YeastSeeder
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext

object DatabaseInitializer {

    suspend fun initializeIfNeeded(context: Context, database: BeerworksDatabase) = withContext(Dispatchers.IO) {
        val gson = Gson()
        val seeders: List<DomainSeeder> = listOf(
            StyleSeeder(
                bjcpDao = database.bjcpStyleDao(),
                baDao = database.baStyleDao(),
                meadDao = database.meadStyleDao(),
                ciderDao = database.ciderStyleDao()
            ),
            HopSeeder(database.hopDao(), database.hopStyleCrossRefDao()),
            MaltSeeder(database.maltDao(), database.maltStyleCrossRefDao()),
            HoneySeeder(database.honeyDao(), database.honeyStyleCrossRefDao()),
            PomeSeeder(database.pomeDao(), database.pomeStyleCrossRefDao()),
            YeastSeeder(database.yeastDao(), database.yeastStyleCrossRefDao())
        )
        coroutineScope {
            seeders.map { seeder ->
                async { seeder.seedIfNeeded(context.assets, gson) }
            }.awaitAll()
        }
    }
}
