package cz.geek.spdreport.auth

import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal

interface User : OAuth2AuthenticatedPrincipal {

    fun getIcon(): String?

    fun isIcalSupported(): Boolean = false
}
