package cz.geek.spdreport.service

import cz.geek.spdreport.model.Country
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

data class Report(
    val name: String,
    val number: String,
    val country: Country,
    val items: List<ReportItem>,
    val incidents: List<ReportIncident> = emptyList(),
)

data class ReportItem(
    val date: LocalDate,
    val start: LocalTime,
    val end: LocalTime,
)

data class ReportIncident(
    val number: Int,
    val title: String,
    val opened: LocalDateTime,
    val closed: LocalDateTime?,
)
