package cz.geek.spdreport.service

import cz.geek.spdreport.model.DateRange
import cz.geek.spdreport.model.EmailFrequency
import cz.geek.spdreport.model.ReportData
import cz.geek.spdreport.model.Settings
import cz.geek.spdreport.datastore.SettingsRepository
import jakarta.mail.internet.InternetAddress
import mu.KotlinLogging
import org.springframework.boot.autoconfigure.mail.MailProperties
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service
import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.Context
import java.net.URL
import java.time.format.DateTimeFormatter

private val logger = KotlinLogging.logger {}

@Service
class EmailService(
    private val reportService: ReportService,
    private val sender: JavaMailSender,
    private val engine: TemplateEngine,
    private val settingsRepository: SettingsRepository,
    mailProperties: MailProperties,
) {

    private val from = InternetAddress(mailProperties.username, "S PD Report")

    fun sendReport(user: String) {
        settingsRepository.load(user)
            .let { requireNotNull(it) { "No settings found for $user" } }
            .also(this::sendReport)
    }

    private fun sendReport(settings: Settings) {
        val freq = settings.emailFrequency
        val dateRange = freq.toDateRange()
        val reportData = settings.toReportData(dateRange)
        val url = reportData.url
        val email = settings.email
        if (url != null && email != null) {
            logger.info {
                "Sending email $freq to $email"
            }
            try {
                sendReport(reportData, url, Email(email, settings.fullName, freq, dateRange))
            } catch (e: Exception) {
                logger.error(e) { "Failed to send to $email" }
            }
        }
    }

    private fun sendReport(reportData: ReportData, url: URL, email: Email): Unit =
        createReport(reportData, url)
            .let {
                sendReport(email, it)
            }

    private fun sendReport(email: Email, data: String): Unit =
        sender.send { mimeMessage ->
            MimeMessageHelper(mimeMessage)
                .apply {
                    setFrom(from)
                    setTo(email.recipientAddress)
                    setSubject(email.subject)
                    setText(data, true)
                }
        }

    fun createReport(reportData: ReportData, url: URL): String =
        reportService.create(reportData, url)
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

private val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")

data class Email(
    val recipient: String,
    val fullName: String?,
    val freq: EmailFrequency,
    val dateRange: DateRange,
) {
    val recipientAddress: InternetAddress
        get() = if (fullName == null) InternetAddress(recipient) else InternetAddress(recipient, fullName)

    val subject: String
        get() = "S PD Report: ${freq.title} ($start - $end)"

    private val start: String
        get() = formatter.format(dateRange.start)

    private val end: String
        get() = formatter.format(dateRange.end)

}
