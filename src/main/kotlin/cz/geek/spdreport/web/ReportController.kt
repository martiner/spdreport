package cz.geek.spdreport.web

import cz.geek.spdreport.model.DateRanges.PREVIOUS_MONTH
import cz.geek.spdreport.model.ReportData
import cz.geek.spdreport.service.ReportService
import cz.geek.spdreport.model.Settings
import cz.geek.spdreport.datastore.SettingsRepository
import cz.geek.spdreport.model.fullName
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/")
class ReportController(
    private val service: ReportService,
    private val settingsRepository: SettingsRepository,
) {

    @GetMapping
    fun get(): String = "report"

    @PostMapping
    fun post(
        @ModelAttribute reportData: ReportData,
        model: Model,
        @AuthenticationPrincipal principal: OAuth2AuthenticatedPrincipal?
    ): String {
        model.addAttribute("list", service.create(reportData))
        if (principal != null) {
            settingsRepository.load(principal.name) ?: Settings(principal)
                .apply {
                    fullName = reportData.name
                    number = reportData.number
                    url = reportData.url.toString()
                }
                .also {
                    settingsRepository.save(it)
                }
        }
        return "report"
    }

    @ModelAttribute
    fun model(@AuthenticationPrincipal principal: OAuth2AuthenticatedPrincipal?): ReportData {
        if (principal != null) {
            val settings = settingsRepository.load(principal.name)
            if (settings != null) {
                return settings.toReportData()
            }
        }
        return PREVIOUS_MONTH.dateRange()
            .let {
                ReportData(
                    name = principal.fullName() ?: "",
                    number = "",
                    start = it.start,
                    end = it.end,
                )
            }
    }
}
