package com.bc.beerworks.data.local.seeder

import android.content.res.AssetManager
import androidx.sqlite.db.SupportSQLiteDatabase
import com.bc.beerworks.data.local.MIGRATION_15_16
import com.bc.beerworks.data.local.dao.HoneyDao
import com.bc.beerworks.data.local.dao.HoneyStyleCrossRefDao
import com.bc.beerworks.data.local.dao.HopDao
import com.bc.beerworks.data.local.dao.HopStyleCrossRefDao
import com.bc.beerworks.data.local.dao.MaltDao
import com.bc.beerworks.data.local.dao.MaltStyleCrossRefDao
import com.bc.beerworks.data.local.dao.PomeDao
import com.bc.beerworks.data.local.dao.PomeStyleCrossRefDao
import com.bc.beerworks.data.local.dao.YeastDao
import com.bc.beerworks.data.local.dao.YeastStyleCrossRefDao
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import java.io.ByteArrayInputStream
import java.io.File

class DomainSeederTest {

    @Test
    fun styleGuidelineResolver_infersGuidelineFromStylePrefix() {
        assertThat(StyleGuidelineResolver.inferGuideline("21A")).isEqualTo("BJCP_2021")
        assertThat(StyleGuidelineResolver.inferGuideline("M1A")).isEqualTo("BJCP_MEAD")
        assertThat(StyleGuidelineResolver.inferGuideline("M4A")).isEqualTo("BJCP_MEAD")
        assertThat(StyleGuidelineResolver.inferGuideline("C1A")).isEqualTo("BJCP_CIDER")
        assertThat(StyleGuidelineResolver.inferGuideline("C2F")).isEqualTo("BJCP_CIDER")
    }

    @Test
    fun ingredientSeeders_skipAssetParsingWhenTableAlreadyPopulated() = runTest {
        val assets = mock(AssetManager::class.java)
        val gson = Gson()

        val hopDao = mock(HopDao::class.java)
        val hopCrossRefDao = mock(HopStyleCrossRefDao::class.java)
        doReturn(40).`when`(hopDao).getHopCount()

        val maltDao = mock(MaltDao::class.java)
        val maltCrossRefDao = mock(MaltStyleCrossRefDao::class.java)
        doReturn(35).`when`(maltDao).getMaltCount()

        val honeyDao = mock(HoneyDao::class.java)
        val honeyCrossRefDao = mock(HoneyStyleCrossRefDao::class.java)
        doReturn(20).`when`(honeyDao).getHoneyCount()

        val pomeDao = mock(PomeDao::class.java)
        val pomeCrossRefDao = mock(PomeStyleCrossRefDao::class.java)
        doReturn(25).`when`(pomeDao).getPomeCount()

        val yeastDao = mock(YeastDao::class.java)
        val yeastCrossRefDao = mock(YeastStyleCrossRefDao::class.java)
        doReturn(50).`when`(yeastDao).getYeastCount()

        HopSeeder(hopDao, hopCrossRefDao).seedIfNeeded(assets, gson)
        MaltSeeder(maltDao, maltCrossRefDao).seedIfNeeded(assets, gson)
        HoneySeeder(honeyDao, honeyCrossRefDao).seedIfNeeded(assets, gson)
        PomeSeeder(pomeDao, pomeCrossRefDao).seedIfNeeded(assets, gson)
        YeastSeeder(yeastDao, yeastCrossRefDao).seedIfNeeded(assets, gson)

        verifyNoInteractions(assets)
    }

    @Test
    fun hopSeeder_seedsHopsAndBraggotCrossRefsWhenTableEmpty() = runTest {
        val assets = mock(AssetManager::class.java)
        val gson = Gson()
        val hopDao = mock(HopDao::class.java)
        val hopCrossRefDao = mock(HopStyleCrossRefDao::class.java)

        doReturn(0).`when`(hopDao).getHopCount()
        val json = """
            [
              {
                "id": "citra",
                "name": "Citra",
                "origin": "United States",
                "countryCode": "US",
                "purpose": "Aroma",
                "commonStyleIds": ["21A"]
              }
            ]
        """.trimIndent()
        doReturn(ByteArrayInputStream(json.toByteArray())).`when`(assets).open("hops.json")

        HopSeeder(hopDao, hopCrossRefDao).seedIfNeeded(assets, gson)

        org.mockito.Mockito.verify(hopDao).insertAll(org.mockito.Mockito.anyList())
        org.mockito.Mockito.verify(hopCrossRefDao).insertAll(org.mockito.Mockito.anyList())
    }

    @Test
    fun migration_15_to_16_clearsHopAndMaltTablesAndRebuildsFts() {
        val mockDb = mock(SupportSQLiteDatabase::class.java)

        MIGRATION_15_16.migrate(mockDb)

        verify(mockDb).execSQL("DELETE FROM `hop_style_cross_ref`")
        verify(mockDb).execSQL("DELETE FROM `hops`")
        verify(mockDb).execSQL("INSERT INTO `hops_fts`(`hops_fts`) VALUES('rebuild')")
        verify(mockDb).execSQL("DELETE FROM `malt_style_cross_ref`")
        verify(mockDb).execSQL("DELETE FROM `malts`")
        verify(mockDb).execSQL("INSERT INTO `malts_fts`(`malts_fts`) VALUES('rebuild')")
    }

    private data class AssetStyleEntry(val number: String)
    private data class AssetIngredientEntry(
        val id: String,
        val commonStyleIds: List<String> = emptyList()
    )

    @Test
    fun assetCatalogs_haveValidStyleIdsAndExpectedBjcpCoverage() {
        val assetsDir = listOf(File("src/main/assets"), File("app/src/main/assets"))
            .first { it.isDirectory }
        val gson = Gson()
        val styleListType = object : TypeToken<List<AssetStyleEntry>>() {}.type
        val ingredientListType = object : TypeToken<List<AssetIngredientEntry>>() {}.type

        val bjcpStyleNumbers = gson.fromJson<List<AssetStyleEntry>>(
            File(assetsDir, "bjcp_styles.json").readText(),
            styleListType
        ).map { it.number }.toSet()

        val hops = gson.fromJson<List<AssetIngredientEntry>>(
            File(assetsDir, "hops.json").readText(),
            ingredientListType
        )
        val malts = gson.fromJson<List<AssetIngredientEntry>>(
            File(assetsDir, "malts.json").readText(),
            ingredientListType
        )

        val hopMappedStyles = hops.flatMap { it.commonStyleIds }.toSet()
        val maltMappedStyles = malts.flatMap { it.commonStyleIds }.toSet()

        assertThat(hopMappedStyles - bjcpStyleNumbers).isEmpty()
        assertThat(maltMappedStyles - bjcpStyleNumbers).isEmpty()

        val expectedBaseStyleVariableHops = setOf(
            "29A", "29B", "29C", "29D",
            "30A", "30B", "30C", "30D",
            "31A", "31B", "32B", "33A", "33B",
            "34A", "34B", "34C"
        )
        val expectedBaseStyleVariableMalts = setOf(
            "29A", "29B", "29C", "29D",
            "30A", "30B", "30D",
            "31B", "33A", "33B",
            "34A", "34B"
        )

        assertThat(bjcpStyleNumbers - hopMappedStyles).isEqualTo(expectedBaseStyleVariableHops)
        assertThat(bjcpStyleNumbers - maltMappedStyles).isEqualTo(expectedBaseStyleVariableMalts)

        // Regression guards for cleaned-up historical mappings
        val zappaStyles = hops.first { it.id == "zappa" }.commonStyleIds
        val carafaThreeStyles = malts.first { it.id == "carafa_special_three" }.commonStyleIds
        assertThat(zappaStyles).doesNotContain("27A")
        assertThat(carafaThreeStyles).doesNotContain("27E")
    }
}
