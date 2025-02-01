package cz.geek.spdreport.web

import cz.geek.spdreport.datastore.SettingsRepository
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler

class SmartAuthenticationSuccessHandler(
    private val repository: SettingsRepository,
) : SavedRequestAwareAuthenticationSuccessHandler() {

    override fun determineTargetUrl(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ): String =
        if (loggedUserAlreadyHasSettings(authentication.principal as? OAuth2AuthenticatedPrincipal)) {
            ReportController.URL
        } else {
            SettingsController.URL
        }

    private fun loggedUserAlreadyHasSettings(principal: OAuth2AuthenticatedPrincipal?): Boolean =
        principal?.let { repository.load(it) != null } == true
}
