package cz.geek.spdreport.service

import jakarta.mail.internet.InternetAddress
import org.springframework.boot.autoconfigure.mail.MailProperties
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Component

@Component
class EmailSender(
    private val sender: JavaMailSender,
    mailProperties: MailProperties,
) {

    private val from = InternetAddress(mailProperties.username, "S PD Report")

    fun sendEmail(email: Email): Unit =
        sender.send { mimeMessage ->
            MimeMessageHelper(mimeMessage)
                .apply {
                    setFrom(from)
                    setTo(email.recipient)
                    setSubject(email.subject)
                    setText(email.htmlBody, true)
                }
        }

}