package cz.geek.spdreport.service

import cz.geek.spdreport.model.ReportData
import cz.geek.spdreport.model.Settings
import cz.geek.spdreport.datastore.SettingsRepository
import mu.KotlinLogging
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
) {

    fun sendReport(user: String) {
        settingsRepository.load(user)
            .let { requireNotNull(it) { "No settings found for $user" } }
            .also(this::sendReport)
    }

    private fun sendReport(settings: Settings) {
        val email = settings.email
        if (email == null) {
            logger.warn { "No email for ${settings.id}" }
            return
        }
        val freq = settings.emailFrequency
        val dateRange = freq.toDateRange()
        val reportData = settings.toReportData(dateRange)
        if (reportData.url == null) {
            logger.warn { "URL missing for $email" }
            return
        }
        logger.info { "Sending email $freq to $email" }
        try {
            createReport(reportData)
                .let {
                    sender.sendEmail(Email(email, settings.fullName, freq, dateRange, it))
                }
        } catch (e: Exception) {
            logger.error(e) { "Failed to send to $email" }
        }
    }

    private fun createReport(reportData: ReportData): String =
        reportService.create(reportData)
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
