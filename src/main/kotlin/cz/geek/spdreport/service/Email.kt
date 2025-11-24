package cz.geek.spdreport.service

import cz.geek.spdreport.model.DateRange
import cz.geek.spdreport.model.EmailFrequency
import jakarta.mail.internet.InternetAddress
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")

private fun LocalDate.format(): String = formatter.format(this)

data class Email(
    val recipient: InternetAddress,
    val subject: String,
    val htmlBody: String,
) {
    constructor(recipient: String, fullName: String?, freq: EmailFrequency, dateRange: DateRange, htmlBody: String) :
            this(
                if (fullName == null) InternetAddress(recipient) else InternetAddress(recipient, fullName),
                "S PD Report: ${freq.title} (${dateRange.start.format()} - ${dateRange.end.format()})",
                htmlBody,
            )
}
