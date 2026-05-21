package cz.geek.spdreport.datastore

import com.googlecode.objectify.ObjectifyService.ofy
import com.googlecode.objectify.Work
import cz.geek.spdreport.model.ProcessedWebhookEvent
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class ProcessedWebhookEventRepository {

    /**
     * Atomically records [id] as a processed webhook event.
     *
     * @return `true` if the id was newly recorded (first time seen), `false` if an event with the
     * same id had already been recorded (duplicate). The check-and-save runs in a transaction so
     * concurrent retries of the same delivery (e.g. a cold-start burst) yield exactly one `true`.
     */
    fun recordIfNew(id: String): Boolean =
        ofy().transactNew(Work {
            if (load(id) != null) {
                false
            } else {
                ofy().save().entities(ProcessedWebhookEvent(id, Instant.now())).now()
                true
            }
        })

    internal fun load(id: String): ProcessedWebhookEvent? =
        ofy().load().type(ProcessedWebhookEvent::class.java).id(id).now()
}
