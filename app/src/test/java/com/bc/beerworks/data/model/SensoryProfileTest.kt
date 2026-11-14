package com.bc.beerworks.data.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class SensoryProfileTest {

    @Test
    fun `SensoryRadarProfile implements SensoryProfile contract`() {
        val empty = SensoryRadarProfile()
        assertThat(empty.hasData()).isFalse()

        val populated = SensoryRadarProfile(
            citrus = 5f,
            tropical = 4f,
            floral = 2f,
            herbal = 1f,
            spicy = 1f,
            piney = 3f,
            earthy = 0f,
            grassy = 1f
        )
        assertThat(populated.hasData()).isTrue()
        assertThat(populated.defaultStrokeColorHex).isNull()
        assertThat(populated.toAxisMap().map { it.first }).containsExactly(
            "Citrus", "Tropical", "Floral", "Herbal", "Spicy", "Piney", "Earthy", "Grassy"
        )
    }

    @Test
    fun `MaltSensoryProfile implements SensoryProfile contract`() {
        val empty = MaltSensoryProfile()
        assertThat(empty.hasData()).isFalse()

        val populated = MaltSensoryProfile(
            maltySweet = 4f,
            breadyBiscuit = 5f,
            toasty = 3f,
            caramelToffee = 2f
        )
        assertThat(populated.hasData()).isTrue()
        assertThat(populated.defaultStrokeColorHex).isEqualTo(0xFFD97706L)
        assertThat(populated.toAxisMap().map { it.first }).containsExactly(
            "Sweet", "Biscuit", "Toasty", "Caramel", "Nutty", "Chocolate", "Roast", "Husky"
        )
    }

    @Test
    fun `HoneySensoryProfile implements SensoryProfile contract`() {
        val empty = HoneySensoryProfile()
        assertThat(empty.hasData()).isFalse()

        val populated = HoneySensoryProfile(
            floral = 5f,
            fruity = 4f,
            sweetConfectionary = 3f
        )
        assertThat(populated.hasData()).isTrue()
        assertThat(populated.defaultStrokeColorHex).isEqualTo(0xFFE5A910L)
        assertThat(populated.toAxisMap().map { it.first }).containsExactly(
            "Floral", "Fruity", "Herbal", "Spicy", "Sweet", "Woody", "Earthy", "Pungent"
        )
    }

    @Test
    fun `PomeSensoryProfile implements SensoryProfile contract`() {
        val empty = PomeSensoryProfile()
        assertThat(empty.hasData()).isFalse()

        val populated = PomeSensoryProfile(
            aciditySharpness = 4f,
            tanninBitterness = 5f,
            earthyFarmhouse = 3f
        )
        assertThat(populated.hasData()).isTrue()
        assertThat(populated.defaultStrokeColorHex).isEqualTo(0xFF4D7C0FL)
        assertThat(populated.toAxisMap().map { it.first }).containsExactly(
            "Acidity", "Tannin", "Fresh Fruit", "Baked Fruit", "Citrus", "Floral", "Earthy", "Sweet"
        )
    }

    @Test
    fun `YeastSensoryProfile implements SensoryProfile contract`() {
        val empty = YeastSensoryProfile()
        assertThat(empty.hasData()).isFalse()

        val populated = YeastSensoryProfile(
            fruityEsters = 4f,
            spicyPhenols = 5f,
            funkyEarthy = 3f
        )
        assertThat(populated.hasData()).isTrue()
        assertThat(populated.defaultStrokeColorHex).isEqualTo(0xFFD97706L)
        assertThat(populated.toAxisMap().map { it.first }).containsExactly(
            "Clean", "Fruity", "Spicy", "Tart", "Funky", "Sulfury", "Alcohol", "Mouthfeel"
        )
    }
}
