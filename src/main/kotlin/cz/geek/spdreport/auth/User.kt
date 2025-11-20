package cz.geek.spdreport.auth

import org.springframework.security.core.AuthenticatedPrincipal

interface User : AuthenticatedPrincipal {

    fun getIcon(): String?
}
