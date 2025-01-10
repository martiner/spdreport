package cz.geek.spdreport.web

import cz.geek.spdreport.ItHelper.httpPort
import io.kotest.assertions.assertSoftly
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import io.kotest.mpp.log
import mu.KotlinLogging
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.client.getForEntity
import org.springframework.boot.web.client.RestTemplateBuilder

private val logger = KotlinLogging.logger {}

class WebIT : FreeSpec({

    val root = "http://localhost:$httpPort/"
    logger.info { "Configured REST template for $root" }

    val rest = RestTemplateBuilder()
        .rootUri(root)
        .let { TestRestTemplate(it) }

    "Should return 200" {
        assertSoftly(rest.getForEntity<String>("/")) {
            statusCode.value() shouldBe 200
        }
    }
})
