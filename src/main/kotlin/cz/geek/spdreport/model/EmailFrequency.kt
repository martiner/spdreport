package cz.geek.spdreport.model

import org.springframework.core.io.ClassPathResource
import org.yaml.snakeyaml.LoaderOptions
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.constructor.Constructor
import java.nio.file.Path
import java.time.LocalDate
import kotlin.io.path.name

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

    companion object {
        val SCHEDULE: Map<EmailFrequency, String?>  =
            Yaml(Constructor(CronConfig::class.java, LoaderOptions()))
                .run {
                    load<CronConfig>(ClassPathResource("cron.yaml").inputStream)
                }
                .run {
                    cron.mapNotNull { it.asPair() }.toMap()
                }
                .run {
                    EmailFrequency.entries.associateWith { this[it.name] }
                }
    }
}

data class CronConfig(
    var cron: List<CronJob> = listOf(),
)

data class CronJob(
    var url: String? = null,
    var schedule: String? = null,
    var timezone: String? = null,
    var description: String? = null,
) {
    fun asPair(): Pair<String, String>? =
        if (url != null && schedule != null) (Path.of(url!!).name.uppercase() to schedule!!) else null
}
