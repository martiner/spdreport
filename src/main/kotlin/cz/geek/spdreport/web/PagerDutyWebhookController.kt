package cz.geek.spdreport.web

import cz.geek.spdreport.pagerduty.IncidentWebhookPayload
import cz.geek.spdreport.service.PagerDutyWebhookService
import org.springframework.http.HttpStatus.OK
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/webhooks/pagerduty")
class PagerDutyWebhookController(
    private val service: PagerDutyWebhookService,
) {

    @ResponseStatus(OK)
    @PostMapping("/incident")
    fun incident(payload: IncidentWebhookPayload) {
        service.handle(payload)
    }
}
