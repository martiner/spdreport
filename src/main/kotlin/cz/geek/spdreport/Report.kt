package cz.geek.spdreport

import java.time.LocalDate
import java.time.LocalTime

data class Report(
    val date: LocalDate,
    val start: LocalTime,
    val end: LocalTime,
    val name: String,
    val number: String,
)
