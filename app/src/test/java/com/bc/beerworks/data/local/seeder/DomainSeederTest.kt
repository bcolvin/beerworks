package com.bc.beerworks.data.local.seeder

import android.content.res.AssetManager
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
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.mock
import org.mockito.Mockito.verifyNoInteractions
import java.io.ByteArrayInputStream

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
}
