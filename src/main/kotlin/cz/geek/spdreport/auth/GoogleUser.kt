package cz.geek.spdreport.auth

import org.springframework.security.oauth2.core.oidc.user.OidcUser

class GoogleUser(private val user: OidcUser) : User, OidcUser by user {

    override fun getIcon(): String? = attributes["picture"] as? String
}
