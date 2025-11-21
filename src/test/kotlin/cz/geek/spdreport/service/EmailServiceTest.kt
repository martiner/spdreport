package cz.geek.spdreport.service

import com.ninjasquad.springmockk.MockkBean
import cz.geek.spdreport.datastore.OAuth2AuthorizedClientRepository
import cz.geek.spdreport.datastore.SettingsRepository
import cz.geek.spdreport.model.Country
import cz.geek.spdreport.model.EmailFrequency
import cz.geek.spdreport.model.ObjectifyOAuth2AuthorizedClient
import cz.geek.spdreport.model.Settings
import io.kotest.assertions.assertSoftly
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import io.mockk.CapturingSlot
import io.mockk.Runs
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.just
import io.mockk.slot
import org.springframework.boot.test.context.SpringBootTest
import java.time.LocalDate
import java.time.LocalTime

@SpringBootTest
class EmailServiceTest(
    emailService: EmailService,
    @MockkBean val reportService: ReportService,
    @MockkBean val sender: EmailSender,
    @MockkBean val repository: SettingsRepository,
    @MockkBean val clientRepository: OAuth2AuthorizedClientRepository,
) : FreeSpec({

    lateinit var email: CapturingSlot<Email>

    val settings = Settings(
        id = "jamesb",
        fullName = "James Bond",
        number = "007",
        email = "james@example.com",
        country = Country.CZ,
        emailFrequency = EmailFrequency.MONTHLY,
        url = "http://foo",
    )

    val report = Report(
        date = LocalDate.of(2023, 9, 11),
        start = LocalTime.of(17, 0),
        end = LocalTime.of(23, 0),
        name = settings.fullName!!,
        number = settings.number!!,
        country = settings.country!!,
    )

    beforeTest {
        email = slot()
        every { sender.sendEmail(capture(email)) } just Runs
        every { repository.loadOrThrow(settings.id) } returns settings
        every { clientRepository.load(settings.id) } returns
                ObjectifyOAuth2AuthorizedClient(
                    principalName = settings.id,
                    clientId = "169396583635-tf1ld3l5skipq2h5gbqko57idvmve0oj.apps.googleusercontent.com", // google
                )
    }
    afterTest {
        clearMocks(sender, repository, clientRepository)
    }

    "Should create report for iCal" {
        every { reportService.create(any(), null) } returns listOf(report)

        emailService.sendReport("jamesb")

        email.isCaptured shouldBe true
        assertSoftly(email.captured.htmlBody) { html ->
            html shouldContain "Monday, Sep 11, 2023"
            html shouldContain settings.number!!
            html shouldContain "17:00"
            html shouldContain "23:00"
            html shouldNotContain "No data during the period"
        }
    }

    "Should create report for PagerDuty" {
        val pdSettings = settings.copy(id = "jamesbpd", url = null)
        every { reportService.create(any(), match { it.name == "pdprinc" } ) } returns listOf(report)
        every { repository.loadOrThrow(pdSettings.id) } returns pdSettings
        every { clientRepository.load(pdSettings.id) } returns
                ObjectifyOAuth2AuthorizedClient(
                    principalName = "pdprinc",
                    clientId = "66a97c2f-0a28-4a28-a79d-a7042a877971", // pagerduty
                )

        emailService.sendReport("jamesbpd")

        email.isCaptured shouldBe true
        assertSoftly(email.captured.htmlBody) { html ->
            html shouldContain "Monday, Sep 11, 2023"
            html shouldContain settings.number!!
            html shouldContain "17:00"
            html shouldContain "23:00"
            html shouldNotContain "No data during the period"
        }
    }

    "Should create empty report" {
        every { reportService.create(any(), null) } returns listOf()

        emailService.sendReport("jamesb")

        email.isCaptured shouldBe true
        assertSoftly(email.captured.htmlBody) { html ->
            html shouldContain "No data during the period"
        }
    }
})
