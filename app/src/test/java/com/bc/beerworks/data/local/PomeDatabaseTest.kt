package com.bc.beerworks.data.local

import androidx.sqlite.db.SupportSQLiteDatabase
import com.bc.beerworks.data.local.entity.PomeVarietyEntity
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.ArgumentMatchers.contains
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

class PomeDatabaseTest {

    @Test
    fun pomeVarietyEntity_toDomainModel_mapsAttributesCorrectly() {
        val entity = PomeVarietyEntity(
            id = "dabinett",
            name = "Dabinett",
            fruitType = "Apple",
            classification = "Bittersweet",
            fruitUse = "Heritage Cider/Perry",
            botanicalName = "Malus domestica",
            origin = "Somerset, England",
            countryCode = "GB",
            acidityMalicPercent = 0.18,
            tanninPolyphenolPercent = 0.35,
            tanninLevel = "High",
            sgMin = 1.050,
            sgMax = 1.058,
            harvestSeason = "Late",
            sorbitolGramsPerLiter = null,
            description = "English bittersweet cider apple.",
            blendingGuidance = "Outstanding as a single-varietal cider.",
            pressingCharacteristics = "Presses easily with good juice yields.",
            descriptors = "Soft Tannin, Baked Apple, Vanilla",
            substituteIds = "yarlington-mill, chisel-jersey",
            radarAcidity = 1.5f,
            radarTannin = 3.8f,
            radarFreshFruit = 3.0f,
            radarBakedFruit = 4.0f,
            radarCitrus = 1.2f,
            radarFloral = 2.2f,
            radarEarthy = 2.8f,
            radarSweet = 3.5f
        )

        val domain = entity.toDomainModel()

        assertThat(domain.id).isEqualTo("dabinett")
        assertThat(domain.name).isEqualTo("Dabinett")
        assertThat(domain.fruitType).isEqualTo("Apple")
        assertThat(domain.classification).isEqualTo("Bittersweet")
        assertThat(domain.fruitUse).isEqualTo("Heritage Cider/Perry")
        assertThat(domain.botanicalName).isEqualTo("Malus domestica")
        assertThat(domain.origin).isEqualTo("Somerset, England")
        assertThat(domain.countryCode).isEqualTo("GB")
        assertThat(domain.countryFlag).isEqualTo("🇬🇧")
        assertThat(domain.acidityMalicPercent).isEqualTo(0.18)
        assertThat(domain.tanninPolyphenolPercent).isEqualTo(0.35)
        assertThat(domain.tanninLevel).isEqualTo("High")
        assertThat(domain.sgMin).isEqualTo(1.050)
        assertThat(domain.sgMax).isEqualTo(1.058)
        assertThat(domain.harvestSeason).isEqualTo("Late")
        assertThat(domain.sorbitolGramsPerLiter).isNull()
        assertThat(domain.descriptors).containsExactly("Soft Tannin", "Baked Apple", "Vanilla")
        assertThat(domain.substituteIds).containsExactly("yarlington-mill", "chisel-jersey")
        assertThat(domain.radarProfile).isNotNull
        assertThat(domain.radarProfile?.aciditySharpness).isEqualTo(1.5f)
        assertThat(domain.radarProfile?.tanninBitterness).isEqualTo(3.8f)
        assertThat(domain.sgRangeText).isEqualTo("1.050 – 1.058 SG")
        assertThat(domain.acidityText).isEqualTo("0.18%")
        assertThat(domain.tanninText).isEqualTo("0.35% (High)")
    }

    @Test
    fun pomeVarietyEntity_pearWithSorbitol_mapsSorbitolCorrectly() {
        val entity = PomeVarietyEntity(
            id = "thorn",
            name = "Thorn",
            fruitType = "Pear",
            classification = "Bittersharp",
            fruitUse = "Heritage Cider/Perry",
            botanicalName = "Pyrus communis",
            origin = "Gloucestershire, England",
            countryCode = "GB",
            acidityMalicPercent = 0.50,
            tanninPolyphenolPercent = 0.22,
            tanninLevel = "Medium",
            sgMin = 1.055,
            sgMax = 1.065,
            harvestSeason = "Mid",
            sorbitolGramsPerLiter = 32.0,
            description = "Premier English perry pear.",
            blendingGuidance = "Exquisite single-varietal perry.",
            pressingCharacteristics = "Narrow harvest window, rapid browning.",
            descriptors = "Elderflower, Citrus, Ripe Pear",
            substituteIds = "yellow-huffcap, hendre-huffcap",
            radarAcidity = 3.5f,
            radarTannin = 3.0f,
            radarFreshFruit = 4.4f,
            radarBakedFruit = 3.2f,
            radarCitrus = 3.4f,
            radarFloral = 4.5f,
            radarEarthy = 2.0f,
            radarSweet = 3.5f
        )

        val domain = entity.toDomainModel()

        assertThat(domain.id).isEqualTo("thorn")
        assertThat(domain.fruitType).isEqualTo("Pear")
        assertThat(domain.classification).isEqualTo("Bittersharp")
        assertThat(domain.sorbitolGramsPerLiter).isEqualTo(32.0)
    }

    @Test
    fun migration_7_to_8_executesExpectedDdlStatements() {
        val mockDb = mock(SupportSQLiteDatabase::class.java)

        MIGRATION_7_8.migrate(mockDb)

        verify(mockDb).execSQL(contains("CREATE TABLE IF NOT EXISTS `pomes`"))
        verify(mockDb).execSQL(contains("CREATE VIRTUAL TABLE IF NOT EXISTS `pomes_fts` USING FTS4"))
        verify(mockDb).execSQL(contains("CREATE TABLE IF NOT EXISTS `pome_style_cross_ref`"))
    }
}
