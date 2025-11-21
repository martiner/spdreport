package cz.geek.spdreport.auth

import cz.geek.spdreport.pagerduty.PagerDutyClient
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService
import org.springframework.security.oauth2.core.OAuth2AuthenticationException
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service

@Service
class PagerDutyUserService(
    private val pagerDutyClient: PagerDutyClient,
) : OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    override fun loadUser(req: OAuth2UserRequest): OAuth2User =
        if (req.clientRegistration.registrationId == "pagerduty") {
            pagerDutyClient.fetchCurrentUser(req.accessToken.tokenValue)
                .let { PagerDutyUser(it) }
        } else {
            throw OAuth2AuthenticationException("Unsupported client registration id: ${req.clientRegistration.registrationId}")
        }
}