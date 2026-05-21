package cz.geek.spdreport

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
class SpdreportApplication

fun main(args: Array<String>) {
	runApplication<SpdreportApplication>(*args)
}
