package cz.geek.spdreport.model

import com.googlecode.objectify.annotation.Entity
import com.googlecode.objectify.annotation.Id
import com.googlecode.objectify.annotation.Index
import cz.geek.spdreport.model.DateRanges.PREVIOUS_MONTH
import cz.geek.spdreport.model.EmailFrequency.NONE
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

    constructor(principal: OAuth2AuthenticatedPrincipal, reportData: ReportData) : this(principal) {
        fullName = reportData.name
        number = reportData.number
        url = reportData.url.toString()
    }

    fun toReportData(dateRange: DateRange = defaultRange.dateRange()): ReportData =
        ReportData(
            name = fullName ?: "",
            number = number ?: "",
            start = dateRange.start,
            end = dateRange.end,
            url = URL(url),
        )

    companion object {
        fun Settings.toSettingsData(): SettingsData =
            SettingsData(
                fullName = fullName,
                number = number,
                email = email,
                url = runCatching {  URL(url) } .getOrNull(),
                defaultRange = defaultRange,
                emailFrequency = emailFrequency,
            )
    }
}
