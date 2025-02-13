package cz.geek.spdreport.model

import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal

fun OAuth2AuthenticatedPrincipal?.fullName(): String? =
    this?.getAttribute<String>("name")

fun OAuth2AuthenticatedPrincipal?.email(): String? =
    this?.getAttribute<String>("email")
