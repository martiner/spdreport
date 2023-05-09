package cz.geek.spdreport

import java.time.DayOfWeek.SATURDAY
import java.time.DayOfWeek.SUNDAY
import java.time.LocalDate
import java.time.LocalDateTime
typealias DatePair = Pair<LocalDateTime, LocalDateTime>

class DatePairGenerator(
    private val start: LocalDateTime,
    private val end: LocalDateTime,
) {

    init {
        require(start.isBefore(end)) { "Start date $start is not before end date $end" }
    }

    private val list: MutableList<DatePair> = mutableListOf()

    private fun generate(): List<DatePair> {
        if (start.toLocalDate() == end.toLocalDate()) {
            list.addDatePair(start, end)
        } else {
            list.addDatePair(start, start.toLocalDate().atEndOfDay())
            generate(start.toLocalDate().plusDays(1))
        }
        return list
    }

    private fun generate(startDate: LocalDate) {
        if (startDate == end.toLocalDate()) {
            list.addDatePair(startDate.atStartOfDay(), end)
        } else {
            list.addDatePair(startDate.atStartOfDay(), startDate.atEndOfDay())
            generate(startDate.plusDays(1))
        }
    }

    companion object {
        fun generate(start: LocalDateTime, end: LocalDateTime): List<DatePair> =
            DatePairGenerator(start, end).generate()
    }

    private fun LocalDate.atEndOfDay() = atTime(23, 59)

    private fun MutableList<DatePair>.addDatePair(start: LocalDateTime, end: LocalDateTime) {
        if (start.isWeekend() || (start.hour < 9 && end.hour <= 9) || (start.hour >= 17 && end.hour > 17)) {
            addIfNotSame(start, end)
        } else {
            addIfNotSame(start, start.withHour(9))
            addIfNotSame(start.withHour(17), end)
        }
    }

    private fun MutableList<DatePair>.addIfNotSame(start: LocalDateTime, end: LocalDateTime) {
        if (start != end) {
            add(DatePair(start, end))
        }
    }

    private fun LocalDateTime.isWeekend(): Boolean = dayOfWeek == SUNDAY || dayOfWeek == SATURDAY
}
