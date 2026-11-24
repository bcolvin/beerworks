package com.bc.beerworks.ui.settings

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.time.LocalDate

class BjcpJudgeLinksTest {

    @Test
    fun buildCompetitionCalendarUrl_formatsIsoLocalDateInQueryParam() {
        val date = LocalDate.of(2026, 11, 19)

        val url = BjcpJudgeLinks.buildCompetitionCalendarUrl(date)

        assertThat(url).isEqualTo("https://app.bjcp.org/competition-calendar?start_date=2026-11-19")
    }

    @Test
    fun buildCompetitionCalendarUrl_zeroPadsSingleDigitMonthAndDay() {
        val date = LocalDate.of(2027, 3, 5)

        val url = BjcpJudgeLinks.buildCompetitionCalendarUrl(date)

        assertThat(url).isEqualTo("https://app.bjcp.org/competition-calendar?start_date=2027-03-05")
    }

    @Test
    fun staticBjcpResourceUrls_matchExpectedEndpoints() {
        assertThat(BjcpJudgeLinks.UPCOMING_TASTING_EXAMS_URL)
            .isEqualTo("https://app.bjcp.org/upcoming-tasting-exams")
        assertThat(BjcpJudgeLinks.ONLINE_ENTRANCE_EXAM_PORTAL_URL)
            .isEqualTo("https://app.bjcp.org/")
        assertThat(BjcpJudgeLinks.BJCP_MAIN_WEBSITE_URL)
            .isEqualTo("https://www.bjcp.org/")
        assertThat(BjcpJudgeLinks.BEERWORKS_GITHUB_URL)
            .isEqualTo("https://github.com/bcolvin/beerworks")
        assertThat(BjcpJudgeLinks.PRIVACY_POLICY_URL)
            .isEqualTo("https://github.com/bcolvin/beerworks/blob/master/PRIVACY.md")
    }

    @Test
    fun buildSuggestComponentIssueUrl_encodesCategoryNameAndDetailsIntoGithubIssueUrl() {
        val url = BjcpJudgeLinks.buildSuggestComponentIssueUrl(
            category = "Hop",
            itemName = "Riwaka Cryo",
            details = "NZ Hops, tropical citrus"
        )

        assertThat(url).startsWith("https://github.com/bcolvin/beerworks/issues/new?")
        val decoded = URLDecoder.decode(url, StandardCharsets.UTF_8.name())
        assertThat(decoded).contains("title=[Hop] Missing / Update: Riwaka Cryo")
        assertThat(decoded).contains("- **Category:** Hop")
        assertThat(decoded).contains("- **Name:** Riwaka Cryo")
        assertThat(decoded).contains("NZ Hops, tropical citrus")
    }

    @Test
    fun buildSuggestComponentIssueUrl_handlesBlankNameAndDetailsWithPlaceholders() {
        val url = BjcpJudgeLinks.buildSuggestComponentIssueUrl(
            category = "Yeast",
            itemName = "   ",
            details = ""
        )

        val decoded = URLDecoder.decode(url, StandardCharsets.UTF_8.name())
        assertThat(decoded).contains("title=[Yeast] Missing / Update request")
        assertThat(decoded).contains("- **Category:** Yeast")
        assertThat(decoded).contains("_(please enter name)_")
    }
}
