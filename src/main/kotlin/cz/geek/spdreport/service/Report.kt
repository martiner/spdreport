package cz.geek.spdreport.service

import cz.geek.spdreport.model.Country
import java.time.LocalDate
import java.time.LocalTime

data class Report(
    val name: String,
    val number: String,
    val country: Country,
    val items: List<ReportItem>,
)

data class ReportItem(
    val date: LocalDate,
    val start: LocalTime,
    val end: LocalTime,
)
