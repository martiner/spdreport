package cz.geek.spdreport

import org.apache.commons.lang3.RandomStringUtils
import org.springframework.security.oauth2.core.user.DefaultOAuth2User

object TestHelper {

    val random = RandomStringUtils.insecure()

    fun oAuth2User(userId: String, email: String? = null) =
        buildMap {
            put("id", userId)
            if (email != null) put("email", email)
        }.let {
            DefaultOAuth2User(emptySet(), it, "id")
        }

}
