package cz.geek.spdreport.model

import com.googlecode.objectify.annotation.Entity
import com.googlecode.objectify.annotation.Id
import java.time.Instant

@Entity
data class ProcessedWebhookEvent(
    @Id
    var id: String,
    var receivedAt: Instant = Instant.EPOCH,
) {
    constructor() : this("")
}

