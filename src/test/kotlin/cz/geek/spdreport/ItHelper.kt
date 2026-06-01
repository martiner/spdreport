package cz.geek.spdreport

import com.googlecode.objectify.ObjectifyFactory

object ItHelper {
    val httpPort = System.getProperty("http.port")?.toInt() ?: 8080

    fun ObjectifyFactory.objectify(block: () -> Unit) = begin().use { block() }
}
