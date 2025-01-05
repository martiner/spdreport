package cz.geek.spdreport.service

import com.ninjasquad.springmockk.MockkBean
import cz.geek.spdreport.model.ReportData
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import io.mockk.every
import org.springframework.boot.test.context.SpringBootTest
import java.net.URL
import java.time.LocalDate
import java.time.LocalTime

@SpringBootTest
class EmailServiceTest(
    emailService: EmailService,
    @MockkBean val reportService: ReportService,
) : FreeSpec({

    val url = URL("http://foo")
    val reportData = ReportData(
        "GI Joe",
        "123",
        LocalDate.of(2023, 9, 11),
        LocalDate.of(2023, 9, 11),
    )

    "Should create report" {
        val report = Report(
            reportData.start,
            LocalTime.of(17, 0),
            LocalTime.of(23, 0),
            reportData.name,
            reportData.number,
        )
        every { reportService.create(any(), any<URL>()) } returns listOf(report)
        val html = emailService.createReport(reportData, url)
        html shouldContain "Monday, Sep 11, 2023"
        html shouldContain "123"
        html shouldContain "17:00"
        html shouldContain "23:00"
        html shouldNotContain "No data during the period"
    }

    "Should create empty report" {
        every { reportService.create(any(), any<URL>()) } returns listOf()
        val html = emailService.createReport(reportData, url)
        html shouldContain "No data during the period"
    }

})
