package cz.geek.spdreport.datastore

import com.googlecode.objectify.ObjectifyFactory
import cz.geek.spdreport.model.ObjectifyOAuth2AuthorizedClient
import org.springframework.stereotype.Component

@Component
class OAuth2AuthorizedClientRepository(
    private val factory: ObjectifyFactory,
) {

    fun save(auth: ObjectifyOAuth2AuthorizedClient) {
        factory.ofy().save().entities(auth).now()
    }

    fun load(principalName: String): ObjectifyOAuth2AuthorizedClient? =
        factory.ofy().load().type(ObjectifyOAuth2AuthorizedClient::class.java).id(principalName).now()

    fun delete(principalName: String) {
        factory.ofy().delete().type(ObjectifyOAuth2AuthorizedClient::class.java).id(principalName).now()
    }
}
