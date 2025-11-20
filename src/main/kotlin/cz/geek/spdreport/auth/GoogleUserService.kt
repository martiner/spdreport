package cz.geek.spdreport.auth

import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService
import org.springframework.security.oauth2.core.oidc.user.OidcUser

class GoogleUserService : OidcUserService() {

    override fun loadUser(userRequest: OidcUserRequest): OidcUser? =
        super.loadUser(userRequest)
            ?.let { GoogleUser(it) }
}
