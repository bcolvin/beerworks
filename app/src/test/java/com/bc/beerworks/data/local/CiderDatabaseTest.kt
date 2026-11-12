package com.bc.beerworks.data.local

import androidx.sqlite.db.SupportSQLiteDatabase
import com.bc.beerworks.data.local.entity.CiderStyleEntity
import com.bc.beerworks.data.model.GuidelineType
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

class CiderDatabaseTest {

    @Test
    fun ciderStyleEntity_toDomainModel_mapsAttributesCorrectly() {
        val entity = CiderStyleEntity(
            id = "C1A",
            name = "New World Cider",
            category = "Standard Cider and Perry",
            categoryNumber = "C1",
            number = "C1A",
            overallImpression = "A refreshing, crisp drink",
            aroma = "Prominent apple character",
            appearance = "Clear to brilliant",
            flavor = "Noticeable apple flavor",
            mouthfeel = "Medium-light to medium body",
            comments = "Formerly Common Cider",
            history = "Modern revival style",
            characteristicIngredients = "Culinary or table apples",
            styleComparison = "Comparable to crisp white wine",
            ogMin = 1.045,
            ogMax = 1.065,
            fgMin = 0.995,
            fgMax = 1.020,
            abvMin = 4.5,
            abvMax = 8.0,
            sweetness = "Dry, Medium-Dry, Medium, Medium-Sweet, Sweet",
            carbonation = "Still, Pétillant, Sparkling",
            strength = "Standard",
            commercialExamples = "Angry Orchard Stone Dry",
            tags = "standard-cider, new-world, crisp"
        )

        val domain = entity.toDomainModel(isFavorite = true)

        assertThat(domain.id).isEqualTo("C1A")
        assertThat(domain.name).isEqualTo("New World Cider")
        assertThat(domain.guidelineType).isEqualTo(GuidelineType.BJCP_CIDER)
        assertThat(domain.isFavorite).isTrue()
        assertThat(domain.ibuMin).isNull()
        assertThat(domain.ibuMax).isNull()
        assertThat(domain.srmMin).isNull()
        assertThat(domain.srmMax).isNull()
        assertThat(domain.fgMin).isEqualTo(0.995)
        assertThat(domain.fgMax).isEqualTo(1.020)
        assertThat(domain.sweetness).isEqualTo("Dry, Medium-Dry, Medium, Medium-Sweet, Sweet")
        assertThat(domain.carbonation).isEqualTo("Still, Pétillant, Sparkling")
        assertThat(domain.strength).isEqualTo("Standard")
        assertThat(domain.tags).containsExactly("standard-cider", "new-world", "crisp")
    }

    @Test
    fun migration_5_to_6_executesExpectedDdlStatements() {
        val mockDb = mock(SupportSQLiteDatabase::class.java)

        MIGRATION_5_6.migrate(mockDb)

        verify(mockDb).execSQL(org.mockito.ArgumentMatchers.contains("CREATE TABLE IF NOT EXISTS `cider_styles`"))
        verify(mockDb).execSQL(org.mockito.ArgumentMatchers.contains("CREATE VIRTUAL TABLE IF NOT EXISTS `cider_styles_fts` USING FTS4"))
    }
}
