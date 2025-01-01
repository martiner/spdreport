package cz.geek.spdreport

import com.googlecode.objectify.annotation.Entity
import com.googlecode.objectify.annotation.Id
import com.googlecode.objectify.annotation.Index
import cz.geek.spdreport.DateRanges.PREVIOUS_MONTH
import cz.geek.spdreport.EmailFrequency.NONE
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal
import java.net.URL

@Entity
data class Settings(
    @Id
    var id: String,
    var fullName: String? = null,
    var number: String? = null,
    var email: String? = null,
    var url: String? = null,
    var defaultRange: DateRanges = PREVIOUS_MONTH,
    @Index
    var emailFrequency: EmailFrequency = NONE,
) {
    constructor() : this("")

    constructor(principal: OAuth2AuthenticatedPrincipal) : this(
        id = principal.name,
        fullName = principal.fullName(),
        email = principal.email(),
    )

    fun toReportData(dateRange: DateRange = defaultRange.dateRange()): ReportData =
        ReportData(
            name = fullName ?: "",
            number = number ?: "",
            start = dateRange.start,
            end = dateRange.end,
            url = URL(url),
        )
}

fun OAuth2AuthenticatedPrincipal?.fullName(): String? =
    this?.getAttribute<String>("name")

fun OAuth2AuthenticatedPrincipal?.email(): String? =
    this?.getAttribute<String>("email")
