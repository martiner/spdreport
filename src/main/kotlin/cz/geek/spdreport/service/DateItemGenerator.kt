package cz.geek.spdreport.service

import java.time.DayOfWeek.SATURDAY
import java.time.DayOfWeek.SUNDAY
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.LocalTime.MIDNIGHT

class DateItemGenerator(
    private val start: LocalDateTime,
    private val end: LocalDateTime,
    private val holidays: Set<LocalDate>,
) {

    init {
        require(start.isBefore(end)) { "Start date $start is not before end date $end" }
    }

    private val list: MutableList<DateItem> = mutableListOf()

    private fun generate(): List<DateItem> {
        if (start.toLocalDate() == end.toLocalDate()) {
            list.addDatePair(start, end.toLocalTime())
        } else {
            list.addDatePair(start, MIDNIGHT)
            generate(start.toLocalDate().plusDays(1))
        }
        return list
    }

    private tailrec fun generate(startDate: LocalDate) {
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
        fun generate(start: LocalDateTime, end: LocalDateTime, holidays: Set<LocalDate> = emptySet()): List<DateItem> =
            DateItemGenerator(start, end, holidays).generate()
    }

    private fun MutableList<DateItem>.addDatePair(start: LocalDateTime, end: LocalTime) {
        if (start.isWeekend() || start.isHoliday()) {
            add(DateItem(start, end))
        } else {
            if (start.hour < 9 && (end.hour >= 9 || end.hour == 0)) {
                add(DateItem(start, LocalTime.of(9, 0)))
            } else if (start.hour < 9 && end.hour < 9) {
                add(DateItem(start, end))
            }
            if (start.hour >= 17 && (end.hour > 17 || end.hour == 0)) {
                add(DateItem(start, end))
            } else if (start.hour < 17 && (end.hour > 17 || end.hour == 0)) {
                add(DateItem(start.withHour(17).withMinute(0), end))
            }
        }
    }

    private fun LocalDateTime.isWeekend(): Boolean = dayOfWeek == SUNDAY || dayOfWeek == SATURDAY

    private fun LocalDateTime.isHoliday(): Boolean = holidays.contains(this.toLocalDate())
}
