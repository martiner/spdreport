package cz.geek.spdreport

import cz.geek.spdreport.auth.User
import org.apache.commons.lang3.RandomStringUtils
import org.springframework.security.oauth2.core.user.DefaultOAuth2User
import org.springframework.security.oauth2.core.user.OAuth2User

object TestHelper {

    val random = RandomStringUtils.insecure()

    fun oAuth2User(userId: String, email: String? = null) =
        buildMap {
            put("id", userId)
            if (email != null) put("email", email)
        }.let {
            TestUser(DefaultOAuth2User(emptySet(), it, "id"))
        }

}

class TestUser(user: DefaultOAuth2User) : User, OAuth2User by user {
    override fun getIcon(): String? = null
}
