package cz.geek.spdreport.service

import cz.geek.spdreport.auth.PagerDutyPrincipal
import cz.geek.spdreport.datastore.OAuth2AuthorizedClientRepository
import cz.geek.spdreport.model.ReportData
import cz.geek.spdreport.model.Settings
import cz.geek.spdreport.datastore.SettingsRepository
import mu.KotlinLogging
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties
import org.springframework.stereotype.Service
import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.Context

private val logger = KotlinLogging.logger {}

@Service
class EmailService(
    private val reportService: ReportService,
    private val engine: TemplateEngine,
    private val settingsRepository: SettingsRepository,
    private val sender: EmailSender,
    private val clientRepository: OAuth2AuthorizedClientRepository,
    properties: OAuth2ClientProperties,
) {

    private val clientId: String = properties.registration["pagerduty"]!!.clientId

    fun sendReport(user: String) {
        settingsRepository.loadOrThrow(user)
            .let { it to loadPagerDutyPrincipal(it.id) }
            .also { (settings, principal) -> sendReport(settings, principal) }
    }

    private fun loadPagerDutyPrincipal(user: String): PagerDutyPrincipal? =
        clientRepository.load(user)
            ?.takeIf { it.clientId == clientId }
            ?.let { PagerDutyPrincipal(it.principalName) }

    private fun sendReport(settings: Settings, principal: PagerDutyPrincipal?) {
        val email = settings.email
        if (email == null) {
            logger.warn { "No email for ${settings.id}" }
            return
        }
        val freq = settings.emailFrequency
        val dateRange = freq.toDateRange()
        val reportData = settings.toReportData(dateRange)
        if (reportData.url == null && principal == null) {
            logger.warn { "URL and PagerDuty principal missing for $email" }
            return
        }
        logger.info { "Sending email $freq to $email" }
        try {
            createReport(reportData, principal)
                .let {
                    sender.sendEmail(Email(email, settings.fullName, freq, dateRange, it))
                }
        } catch (e: Exception) {
            logger.error(e) { "Failed to send to $email" }
        }
    }

    private fun createReport(reportData: ReportData, user: PagerDutyPrincipal?): String =
        reportService.create(reportData, user)
            .let {
                Context()
                    .apply {
                        setVariable("list", it)
                    }
            }
            .let {
                engine.process("email", it)
            }
}
