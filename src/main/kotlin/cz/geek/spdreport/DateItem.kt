package cz.geek.spdreport

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

data class DateItem(
    val day: LocalDate,
    val start: LocalTime,
    val end: LocalTime,
) {
    constructor(start: LocalDateTime, end: LocalTime) : this(start.toLocalDate(), start.toLocalTime(), end)
}
