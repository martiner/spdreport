package cz.geek.spdreport.model

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe

class SettingsTest : FreeSpec({

    val settings = Settings()

    "Should convert null URL to ReportData" {
        settings.toReportData().url shouldBe null
    }

    "Should convert null URL to SettingsData" {
        settings.toSettingsData().url shouldBe null
    }

})
