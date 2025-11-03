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
    var country: Country,
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

    fun active(field: String): String =
        if (source()?.let { it.field == field } ?: (field == "url")) "active" else ""

}

data class ReportSource(val field: String, val resource: Resource)
