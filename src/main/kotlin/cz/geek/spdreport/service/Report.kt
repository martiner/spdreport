package cz.geek.spdreport.service

import cz.geek.spdreport.model.Country
import java.time.LocalDate
import java.time.LocalTime

data class Report(
    val date: LocalDate,
    val start: LocalTime,
    val end: LocalTime,
    val name: String,
    val number: String,
    val country: Country,
)
