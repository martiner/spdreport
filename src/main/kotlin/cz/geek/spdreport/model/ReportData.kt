package cz.geek.spdreport.model

import org.springframework.core.io.ByteArrayResource
import org.springframework.core.io.Resource
import org.springframework.core.io.UrlResource
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
) {
    fun source(): ReportSource? =
        when {
            file?.isEmpty == false -> ReportSource(this::file.name, ByteArrayResource(file!!.bytes))
            url != null -> ReportSource(this::url.name, UrlResource(url!!))
            else -> null
        }
}

data class ReportSource(val field: String, val resource: Resource)
