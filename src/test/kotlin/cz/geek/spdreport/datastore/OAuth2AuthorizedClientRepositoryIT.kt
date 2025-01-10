package cz.geek.spdreport.datastore

import cz.geek.spdreport.TestHelper.random
import cz.geek.spdreport.model.ObjectifyOAuth2AuthorizedClient
import io.kotest.assertions.assertSoftly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

class OAuth2AuthorizedClientRepositoryIT : AbstractObjectifyIT({

    val repo = OAuth2AuthorizedClientRepository()

    "Should save, load and delete" {
        val id = random.nextAlphanumeric(10)
        val auth = ObjectifyOAuth2AuthorizedClient(id, "accessToken", "refreshToken")

        objectify {
            repo.load(id).shouldBeNull()

            repo.save(auth)
            assertSoftly(repo.load(id)) {
                shouldNotBeNull()
                principalName shouldBe auth.principalName
                accessTokenType shouldBe auth.accessTokenType
                accessTokenValue shouldBe auth.accessTokenValue
            }

            repo.delete(id)
            repo.load(id).shouldBeNull()
        }
    }
})
