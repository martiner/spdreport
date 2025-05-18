package cz.geek.spdreport.model

import cz.geek.spdreport.model.DateRanges.PREVIOUS_MONTH
import cz.geek.spdreport.model.EmailFrequency.NONE
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal
import java.net.URL

data class SettingsData(
    var fullName: String? = null,
    var number: String? = null,
    var email: String? = null,
    var url: URL? = null,
    var defaultRange: DateRanges = PREVIOUS_MONTH,
    var emailFrequency: EmailFrequency = NONE,
) {
    constructor(principal: OAuth2AuthenticatedPrincipal) : this(
        fullName = principal.fullName(),
        email = principal.email(),
    )

    fun toSettings(principal: OAuth2AuthenticatedPrincipal): Settings =
        Settings(
            id = principal.name,
            email = principal.email(), // this.email should not be used!
            fullName = fullName,
            number = number,
            url = url.toString(),
            defaultRange = defaultRange,
            emailFrequency = emailFrequency,
        )
}
