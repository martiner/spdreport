package cz.geek.spdreport.auth

import org.springframework.security.oauth2.core.user.DefaultOAuth2User

class PagerDutyUser(user: Map<String, Any>) : User, DefaultOAuth2User(null, user, "id") {

    override fun getIcon(): String? = attributes["avatar_url"] as? String
}