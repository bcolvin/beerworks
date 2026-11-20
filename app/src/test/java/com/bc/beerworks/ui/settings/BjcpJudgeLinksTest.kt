package com.bc.beerworks.ui.settings

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
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
    }
}
