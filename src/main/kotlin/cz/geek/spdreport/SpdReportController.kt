package cz.geek.spdreport

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import java.time.LocalDate

@Controller
@RequestMapping("/")
class SpdReportController(
    val service: ReportService,
) {

    @GetMapping
    fun get(): String = "report"

    @PostMapping
    fun post(@ModelAttribute reportData: ReportData, model: Model): String {
        val url = reportData.url
        if (url != null) {
            service.create(reportData, url)
        }
        return "report"
    }

    @ModelAttribute
    fun model() = ReportData(name = "", number = "", start = LocalDate.now(), end = LocalDate.now())
}
