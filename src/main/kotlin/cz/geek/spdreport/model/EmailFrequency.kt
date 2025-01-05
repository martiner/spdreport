package cz.geek.spdreport.model

import java.time.LocalDate

enum class EmailFrequency {
    NONE {
        override fun toDateRange(localDate: LocalDate): DateRange = error("NONE")
    },
    WEEKLY {
        override fun toDateRange(localDate: LocalDate): DateRange =
                localDate.minusDays(1)
                .let {
                    DateRange(it.minusDays(6), it)
                }
    },
    MONTHLY {
        override fun toDateRange(localDate: LocalDate): DateRange =
            localDate.withDayOfMonth(1).minusDays(1)
                .let {
                    DateRange(it.withDayOfMonth(1), it)
                }
    },
    ;

    abstract fun toDateRange(localDate: LocalDate): DateRange

    fun toDateRange(): DateRange = toDateRange(LocalDate.now())

    val title: String
        get() = name.lowercase()
}
