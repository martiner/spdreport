package cz.geek.spdreport

import org.springframework.format.annotation.DateTimeFormat
import java.net.URL
import java.time.LocalDate
import java.time.LocalDateTime

data class ReportData(
    var name: String,
    var number: String,
    @field:DateTimeFormat(pattern = "yyyy-MM-dd")
    var start: LocalDate,
    @field:DateTimeFormat(pattern = "yyyy-MM-dd")
    var end: LocalDate,
    var url: URL? = null,
) {
    fun start(): LocalDateTime = start.atStartOfDay()
    fun end(): LocalDateTime = end.atTime(23, 59)
}
