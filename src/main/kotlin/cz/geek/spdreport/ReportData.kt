package cz.geek.spdreport

import java.net.URL
import java.time.LocalDate
import java.time.LocalDateTime

data class ReportData(
    var name: String,
    var number: String,
    var start: LocalDate,
    var end: LocalDate,
    var url: URL? = null,
) {
    fun start(): LocalDateTime = start.atStartOfDay()
    fun end(): LocalDateTime = end.atTime(23, 59)
}
