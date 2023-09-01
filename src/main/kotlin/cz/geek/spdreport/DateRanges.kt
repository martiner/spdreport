package cz.geek.spdreport

import java.time.LocalDate

enum class DateRanges {
    PREVIOUS_MONTH {
        override fun dateRange(): DateRange =
            LocalDate.now().withDayOfMonth(1).minusMonths(1)
                .let {
                    DateRange(it, it.plusMonths(1).minusDays(1))
                }
    },
    THIS_MONTH {
        override fun dateRange(): DateRange =
            LocalDate.now().withDayOfMonth(1)
                .let {
                    DateRange(it, it.plusMonths(1).minusDays(1))
                }
    },
    TILL_NOW {
        override fun dateRange(): DateRange =
            DateRange(LocalDate.EPOCH, LocalDate.now())
    }
    ;

    abstract fun dateRange(): DateRange

    val title: String
        get() = name.lowercase().replace('_', ' ')
}

data class DateRange(
    val start: LocalDate,
    val end: LocalDate,
)
