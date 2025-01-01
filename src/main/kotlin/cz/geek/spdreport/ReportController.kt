package cz.geek.spdreport

import cz.geek.spdreport.DateRanges.PREVIOUS_MONTH
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import java.net.URL

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
        val url = reportData.url
        if (url != null) {
            model.addAttribute("list", service.create(reportData, url))
        } else if (reportData.file?.isEmpty == false) {
            model.addAttribute("list", service.create(reportData, reportData.file!!.bytes))
        }
        if (principal != null) {
            settingsRepository.load(principal.name) ?: Settings(principal)
                .apply {
                    fullName = reportData.name
                    number = reportData.number
                    this.url = url.toString()
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
