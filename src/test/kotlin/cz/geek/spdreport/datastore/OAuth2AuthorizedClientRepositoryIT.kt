package cz.geek.spdreport.datastore

import com.googlecode.objectify.ObjectifyFactory
import cz.geek.objectify.ObjectifyTest
import cz.geek.spdreport.ItHelper.objectify
import cz.geek.spdreport.TestHelper.random
import cz.geek.spdreport.model.ObjectifyOAuth2AuthorizedClient
import io.kotest.assertions.assertSoftly
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

@ObjectifyTest
class OAuth2AuthorizedClientRepositoryIT(factory: ObjectifyFactory) : FreeSpec({

    val repo = OAuth2AuthorizedClientRepository(factory)

    "Should save, load and delete" {
        val id = random.nextAlphanumeric(10)
        val auth = ObjectifyOAuth2AuthorizedClient(id, "id-123", "accessToken", "refreshToken")

        factory.objectify {
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
