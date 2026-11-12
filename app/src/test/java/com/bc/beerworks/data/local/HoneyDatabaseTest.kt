package com.bc.beerworks.data.local

import androidx.sqlite.db.SupportSQLiteDatabase
import com.bc.beerworks.data.local.entity.HoneyVarietyEntity
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

class HoneyDatabaseTest {

    @Test
    fun honeyVarietyEntity_toDomainModel_mapsAttributesCorrectly() {
        val entity = HoneyVarietyEntity(
            id = "orange_blossom",
            name = "Orange Blossom Honey",
            floralClassification = "Monofloral",
            botanicalSource = "Citrus sinensis",
            origin = "California, United States",
            countryCode = "US",
            pfundMin = 20.0,
            pfundMax = 45.0,
            usdaColor = "White",
            moistureTypicalPercent = 16.8,
            fgRatio = 1.25,
            crystallizationTendency = "Moderate",
            extractPotentialSg = 1.037,
            flavorIntensity = "Moderate",
            description = "The quintessential varietal honey for traditional meads.",
            fermentationBehavior = "Retains citrus blossom aromas when fermented cool.",
            pairingNotes = "Superb with stone fruits and ginger.",
            descriptors = "Citrus Blossom, Orange Zest, Floral",
            substituteIds = "tupelo, sweet_clover",
            radarFloral = 4.8f,
            radarFruity = 4.2f,
            radarHerbal = 1.5f,
            radarSpicy = 1.0f,
            radarSweet = 3.8f,
            radarWoody = 0.5f,
            radarEarthy = 0.5f,
            radarPungent = 0.0f
        )

        val domain = entity.toDomainModel()

        assertThat(domain.id).isEqualTo("orange_blossom")
        assertThat(domain.name).isEqualTo("Orange Blossom Honey")
        assertThat(domain.floralClassification).isEqualTo("Monofloral")
        assertThat(domain.botanicalSource).isEqualTo("Citrus sinensis")
        assertThat(domain.origin).isEqualTo("California, United States")
        assertThat(domain.countryCode).isEqualTo("US")
        assertThat(domain.countryFlag).isEqualTo("🇺🇸")
        assertThat(domain.pfundMin).isEqualTo(20.0)
        assertThat(domain.pfundMax).isEqualTo(45.0)
        assertThat(domain.usdaColor).isEqualTo("White")
        assertThat(domain.moistureTypicalPercent).isEqualTo(16.8)
        assertThat(domain.fgRatio).isEqualTo(1.25)
        assertThat(domain.crystallizationTendency).isEqualTo("Moderate")
        assertThat(domain.extractPotentialSg).isEqualTo(1.037)
        assertThat(domain.flavorIntensity).isEqualTo("Moderate")
        assertThat(domain.descriptors).containsExactly("Citrus Blossom", "Orange Zest", "Floral")
        assertThat(domain.substituteIds).containsExactly("tupelo", "sweet_clover")
        assertThat(domain.radarProfile).isNotNull
        assertThat(domain.radarProfile?.floral).isEqualTo(4.8f)
        assertThat(domain.radarProfile?.fruity).isEqualTo(4.2f)
        assertThat(domain.pfundRangeText).isEqualTo("20 – 45 mm (White)")
    }

    @Test
    fun migration_6_to_7_executesExpectedDdlStatements() {
        val mockDb = mock(SupportSQLiteDatabase::class.java)

        MIGRATION_6_7.migrate(mockDb)

        verify(mockDb).execSQL(org.mockito.ArgumentMatchers.contains("CREATE TABLE IF NOT EXISTS `honeys`"))
        verify(mockDb).execSQL(org.mockito.ArgumentMatchers.contains("CREATE VIRTUAL TABLE IF NOT EXISTS `honeys_fts` USING FTS4"))
        verify(mockDb).execSQL(org.mockito.ArgumentMatchers.contains("CREATE TABLE IF NOT EXISTS `honey_style_cross_ref`"))
    }
}
