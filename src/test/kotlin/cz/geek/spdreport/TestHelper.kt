package cz.geek.spdreport

import org.springframework.security.oauth2.core.user.DefaultOAuth2User

object TestHelper {

    fun oAuth2User(userId: String) = DefaultOAuth2User(emptySet(), mapOf("id" to userId), "id")

}
