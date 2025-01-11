package cz.geek.spdreport.service

import cz.geek.spdreport.datastore.OAuth2AuthorizedClientRepository
import cz.geek.spdreport.model.ObjectifyOAuth2AuthorizedClient
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
    private val repository: OAuth2AuthorizedClientRepository
) : OAuth2AuthorizedClientService {

    private val clientId: String = properties.registration["google"]!!.clientId

    override fun saveAuthorizedClient(client: OAuth2AuthorizedClient, principal: Authentication) {
        logger.info { "Saving ${principal.name}" }
        val auth = ObjectifyOAuth2AuthorizedClient(principal, client.accessToken, client.refreshToken)
        repository.save(auth)
    }

    override fun <T : OAuth2AuthorizedClient> loadAuthorizedClient(clientId: String, principalName: String): T? {
        logger.info { "Loading $principalName" }
        requireMatchingClientIds(clientId)
        val clientRegistration = requireNotNull(clientRegistrationRepository.findByRegistrationId(clientId)) {
            "Registration not found for client id: $clientId"
        }
        return repository.load(principalName)
            ?.let {
                @Suppress("UNCHECKED_CAST")
                OAuth2AuthorizedClient(
                    clientRegistration,
                    principalName,
                    it.asAccessToken(),
                    it.asRefreshToken(),
                ) as T
            }
    }

    override fun removeAuthorizedClient(clientId: String, principalName: String) {
        logger.info { "Removing $principalName" }
        requireMatchingClientIds(clientId)
        repository.delete(principalName)
    }

    private fun requireMatchingClientIds(clientRegistrationId: String) {
        require(clientRegistrationId == clientId) {
            "Client ids don't match! Expected $clientId actual: $clientRegistrationId"
        }
    }
}
