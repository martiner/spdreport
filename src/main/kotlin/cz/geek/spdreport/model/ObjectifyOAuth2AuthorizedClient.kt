package cz.geek.spdreport.model

import com.googlecode.objectify.annotation.Entity
import com.googlecode.objectify.annotation.Id
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.core.OAuth2AccessToken
import org.springframework.security.oauth2.core.OAuth2AccessToken.TokenType
import org.springframework.security.oauth2.core.OAuth2AccessToken.TokenType.BEARER
import org.springframework.security.oauth2.core.OAuth2RefreshToken
import java.time.Instant

private val SUPPORTED_TOKEN_TYPE = BEARER

private const val SCOPE_DELIMITER = ","

@Entity
data class ObjectifyOAuth2AuthorizedClient(
    @Id
    var principalName: String,
    var clientId: String? = null,
    var accessTokenType: String = "",
    var accessTokenValue: String = "",
    var accessTokenIssued: Instant? = null,
    var accessTokenExpires: Instant? = null,
    var accessTokenScopes: String = "",
    var refreshTokenValue: String? = null,
    var refreshTokenIssued: Instant? = null,
    var createdAt: Instant = Instant.now(),
) {
    constructor() : this("")
    constructor(
        principal: Authentication,
        clientId: String,
        accessToken: OAuth2AccessToken,
        refreshToken: OAuth2RefreshToken?,
    ) : this(
        principalName = principal.name,
        clientId = clientId,
        accessTokenType = accessToken.tokenType.value,
        accessTokenIssued = accessToken.issuedAt,
        accessTokenExpires = accessToken.expiresAt,
        accessTokenScopes = accessToken.scopes.joinToString(SCOPE_DELIMITER),
        accessTokenValue = accessToken.tokenValue,
        refreshTokenIssued = refreshToken?.issuedAt,
        refreshTokenValue = refreshToken?.tokenValue,
    )

    fun asAccessToken(): OAuth2AccessToken =
        OAuth2AccessToken(
            tokenType(),
            accessTokenValue,
            accessTokenIssued,
            accessTokenExpires,
            accessTokenScopes.split(SCOPE_DELIMITER).toSet(),
        )

    private fun tokenType(): TokenType? {
        return if (accessTokenType.equals(SUPPORTED_TOKEN_TYPE.value, ignoreCase = true)) SUPPORTED_TOKEN_TYPE else null
    }

    fun asRefreshToken(): OAuth2RefreshToken? =
        if (refreshTokenValue != null) OAuth2RefreshToken(refreshTokenValue, refreshTokenIssued) else null
}
