package cz.geek.spdreport

import com.googlecode.objectify.ObjectifyService.ofy
import org.springframework.stereotype.Service

@Service
class SettingsRepository {

    fun save(settings: Settings) {
        ofy().save().entities(settings).now()
    }

    fun load(name: String?): Settings? =
        ofy().load().type(Settings::class.java).id(name).now()
}
