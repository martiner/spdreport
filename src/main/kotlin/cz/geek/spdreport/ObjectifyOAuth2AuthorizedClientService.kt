package cz.geek.spdreport

import com.googlecode.objectify.ObjectifyService.ofy
import mu.KotlinLogging
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger {}

@Service
class ObjectifyOAuth2AuthorizedClientService(
    properties: OAuth2ClientProperties,
    private val clientRegistrationRepository: ClientRegistrationRepository,
) : OAuth2AuthorizedClientService {

    private val clientId: String = properties.registration["google"]!!.clientId

    override fun saveAuthorizedClient(client: OAuth2AuthorizedClient, principal: Authentication) {
        logger.info { "Saving ${principal.name}" }
        val auth = ObjectifyOAuth2AuthorizedClient(principal, client.accessToken, client.refreshToken)
        ofy().save().entities(auth).now()
    }

    override fun <T : OAuth2AuthorizedClient> loadAuthorizedClient(clientId: String, principalName: String): T {
        logger.info { "Loading $principalName" }
        requireMatchingClientIds(clientId)
        val clientRegistration = requireNotNull(clientRegistrationRepository.findByRegistrationId(clientId)) {
            "Registration not found for client id: $clientId"
        }
        val auth = ofy().load().type(ObjectifyOAuth2AuthorizedClient::class.java).id(principalName).now()
        @Suppress("UNCHECKED_CAST")
        return OAuth2AuthorizedClient(
            clientRegistration,
            principalName,
            auth.asAccessToken(),
            auth.asRefreshToken(),
        ) as T
    }

    override fun removeAuthorizedClient(clientId: String, principalName: String) {
        logger.info { "Removing $principalName" }
        requireMatchingClientIds(clientId)
        ofy().delete().type(ObjectifyOAuth2AuthorizedClient::class.java).id(principalName).now()
    }

    private fun requireMatchingClientIds(clientRegistrationId: String) {
        require(clientRegistrationId == clientId) {
            "Client ids don't match! Expected $clientId actual: $clientRegistrationId"
        }
    }
}
