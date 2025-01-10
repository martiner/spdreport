package cz.geek.spdreport

object ItHelper {
    const val objectifyProject = "spdreport"
    val objectifyPort = System.getProperty("objectify.port")?.toInt() ?: 8484
    val httpPort = System.getProperty("http.port")?.toInt() ?: 8080
}
