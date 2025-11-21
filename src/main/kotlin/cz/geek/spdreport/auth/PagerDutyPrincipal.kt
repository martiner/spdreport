package cz.geek.spdreport.auth

import org.springframework.security.authentication.AbstractAuthenticationToken

class PagerDutyPrincipal(private val user: String) : AbstractAuthenticationToken(null) {
    init {
        isAuthenticated = true
        details = user
    }

    override fun getPrincipal(): Any = user

    override fun getCredentials(): Any? = null
}