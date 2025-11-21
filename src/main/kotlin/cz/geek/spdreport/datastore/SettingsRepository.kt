package cz.geek.spdreport.datastore

import com.googlecode.objectify.ObjectifyService.ofy
import cz.geek.spdreport.auth.User
import cz.geek.spdreport.model.EmailFrequency
import cz.geek.spdreport.model.ReportData
import cz.geek.spdreport.model.Settings
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal
import org.springframework.stereotype.Service

@Service
class SettingsRepository {

    fun save(settings: Settings) {
        ofy().save().entities(settings).now()
    }

    fun load(principal: OAuth2AuthenticatedPrincipal): Settings? =
        load(principal.name)

    fun load(name: String?): Settings? =
        ofy().load().type(Settings::class.java).id(name).now()

    fun loadOrThrow(user: String): Settings =
        load(user)
            .let { requireNotNull(it) { "No settings found for $user" } }

    fun find(freq: EmailFrequency): List<Settings> =
        ofy().load().type(Settings::class.java).filter(Settings::emailFrequency.name, freq).list()

    fun createIfMissing(principal: User, reportData: ReportData) {
        load(principal) ?: save(Settings(principal, reportData))
    }
}
