package cz.geek.spdreport.datastore

import com.googlecode.objectify.ObjectifyService.ofy
import cz.geek.spdreport.model.ObjectifyOAuth2AuthorizedClient
import org.springframework.stereotype.Component

@Component
class OAuth2AuthorizedClientRepository {

    fun save(auth: ObjectifyOAuth2AuthorizedClient) {
        ofy().save().entities(auth).now()
    }

    fun load(principalName: String): ObjectifyOAuth2AuthorizedClient? =
        ofy().load().type(ObjectifyOAuth2AuthorizedClient::class.java).id(principalName).now()

    fun delete(principalName: String) {
        ofy().delete().type(ObjectifyOAuth2AuthorizedClient::class.java).id(principalName).now()
    }
}
