package cz.geek.spdreport.web

import cz.geek.spdreport.model.DateRanges.PREVIOUS_MONTH
import cz.geek.spdreport.model.ReportData
import cz.geek.spdreport.service.ReportService
import cz.geek.spdreport.model.Settings
import cz.geek.spdreport.datastore.SettingsRepository
import cz.geek.spdreport.model.fullName
import net.fortuna.ical4j.data.ParserException
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import java.io.IOException

private const val VIEW = "report"

@Controller
@RequestMapping("/")
class ReportController(
    private val service: ReportService,
    private val settingsRepository: SettingsRepository,
) {

    @GetMapping
    fun get(): String = VIEW

    @PostMapping
    fun post(
        @ModelAttribute reportData: ReportData, errors: BindingResult,
        model: Model,
        @AuthenticationPrincipal principal: OAuth2AuthenticatedPrincipal?
    ): String {
        if (errors.hasErrors()) {
            return VIEW
        }
        try {
            model.addAttribute("list", service.create(reportData))
        } catch (e: ParserException) {
            errors.rejectValue(reportData, "parser", "Invalid iCal file: $e")
        } catch (e: IOException) {
            errors.rejectValue(reportData, "reading", "Failed to read iCal file: $e")
        }
        if (errors.hasErrors()) {
            return VIEW
        }

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
        return VIEW
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

private fun BindingResult.rejectValue(reportData: ReportData, errorCode: String, message: String): Unit =
    rejectValue(reportData.source()?.field, errorCode, message)
