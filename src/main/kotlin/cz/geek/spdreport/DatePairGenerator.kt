package cz.geek.spdreport

import java.time.DayOfWeek.SATURDAY
import java.time.DayOfWeek.SUNDAY
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.LocalTime.MIDNIGHT

typealias DatePair = Pair<LocalDateTime, LocalTime>

class DatePairGenerator(
    private val start: LocalDateTime,
    private val end: LocalDateTime,
    private val holidays: Set<LocalDate>,
) {

    init {
        require(start.isBefore(end)) { "Start date $start is not before end date $end" }
    }

    private val list: MutableList<DatePair> = mutableListOf()

    private fun generate(): List<DatePair> {
        if (start.toLocalDate() == end.toLocalDate()) {
            list.addDatePair(start, end.toLocalTime())
        } else {
            list.addDatePair(start, MIDNIGHT)
            generate(start.toLocalDate().plusDays(1))
        }
        return list
    }

    private fun generate(startDate: LocalDate) {
        if (startDate == end.toLocalDate()) {
            if (end.toLocalTime() == MIDNIGHT) {
                return
            }
            list.addDatePair(startDate.atStartOfDay(), end.toLocalTime())
        } else {
            list.addDatePair(startDate.atStartOfDay(), MIDNIGHT)
            generate(startDate.plusDays(1))
        }
    }

    companion object {
        fun generate(start: LocalDateTime, end: LocalDateTime, holidays: Set<LocalDate> = emptySet()): List<DatePair> =
            DatePairGenerator(start, end, holidays).generate()
    }

    private fun MutableList<DatePair>.addDatePair(start: LocalDateTime, end: LocalTime) {
        if (start.isWeekend() || start.isHoliday()) {
            add(DatePair(start, end))
        } else {
            if (start.hour < 9 && (end.hour >= 9 || end.hour == 0)) {
                add(DatePair(start, LocalTime.of(9, 0)))
            } else if (start.hour < 9 && end.hour < 9) {
                add(DatePair(start, end))
            }
            if (start.hour >= 17 && (end.hour > 17 || end.hour == 0)) {
                add(DatePair(start, end))
            } else if (start.hour < 17 && (end.hour > 17 || end.hour == 0)) {
                add(DatePair(start.withHour(17).withMinute(0), end))
            }
        }
    }

    private fun LocalDateTime.isWeekend(): Boolean = dayOfWeek == SUNDAY || dayOfWeek == SATURDAY

    private fun LocalDateTime.isHoliday(): Boolean = holidays.contains(this.toLocalDate())
}
