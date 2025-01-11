package cz.geek.spdreport.model

import io.kotest.core.spec.style.FreeSpec
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe
import org.springframework.mock.web.MockMultipartFile
import java.net.URL
import java.time.LocalDate

class ReportDataTest : FreeSpec({

    val data = ReportData(
        name = "James",
        number = "007",
        start = LocalDate.of(2023, 9, 1),
        end = LocalDate.of(2023, 9, 30)
    )

    "Should resolve active" - {
        withData(
            nameFn = { "{$it.first.url}/{$it.first.file}/" },
            data.copy(file = MockMultipartFile("foo", ByteArray(1))) to "file",
            data.copy(url = URL("http://foo")) to "url",
            data to "url",
        ) { (data, expected) ->
            withData("url", "file") { field ->
                data.active(field) shouldBe if (field == expected) "active" else ""
            }
        }
    }
})
