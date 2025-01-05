package cz.geek.spdreport.model

import org.springframework.format.annotation.DateTimeFormat
import org.springframework.web.multipart.MultipartFile
import java.net.URL
import java.time.LocalDate

data class ReportData(
    var name: String,
    var number: String,
    @field:DateTimeFormat(pattern = "yyyy-MM-dd")
    var start: LocalDate,
    @field:DateTimeFormat(pattern = "yyyy-MM-dd")
    var end: LocalDate,
    var url: URL? = null,
    var file: MultipartFile? = null,
)
