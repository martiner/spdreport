package cz.geek.spdreport

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
            list.add(DatePair(start, end))
        } else {
            list.add(DatePair(start, start.toLocalDate().atEndOfDay()))
            generate(start.toLocalDate().plusDays(1))
        }
        return list
    }

    private fun generate(startDate: LocalDate) {
        if (startDate == end.toLocalDate()) {
            list.add(DatePair(startDate.atStartOfDay(), end))
        } else {
            list.add(DatePair(startDate.atStartOfDay(), startDate.atEndOfDay()))
            generate(startDate.plusDays(1))
        }
    }

    companion object {
        fun generate(start: LocalDateTime, end: LocalDateTime): List<DatePair> =
            DatePairGenerator(start, end).generate()
    }

    private fun LocalDate.atEndOfDay() = atTime(23, 59)
}
