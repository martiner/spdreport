package cz.geek.spdreport

import org.apache.commons.lang3.RandomStringUtils
import org.springframework.security.oauth2.core.user.DefaultOAuth2User

object TestHelper {

    val random = RandomStringUtils.insecure()

    fun oAuth2User(userId: String) = DefaultOAuth2User(emptySet(), mapOf("id" to userId), "id")

}
