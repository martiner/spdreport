package cz.geek.spdreport

import cz.geek.spdreport.DateRanges.PREVIOUS_MONTH
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/")
class ReportController(
    val service: ReportService,
) {

    @GetMapping
    fun get(): String = "report"

    @PostMapping
    fun post(@ModelAttribute reportData: ReportData, model: Model): String {
        val url = reportData.url
        if (url != null) {
            model.addAttribute("list", service.create(reportData, url))
        } else if (reportData.file?.isEmpty == false) {
            model.addAttribute("list", service.create(reportData, reportData.file!!.bytes))
        }
        return "report"
    }

    @ModelAttribute
    fun model(): ReportData =
        PREVIOUS_MONTH.dateRange()
            .let {
                ReportData(name = "", number = "", start = it.start, end = it.end,
                )
        }

}
