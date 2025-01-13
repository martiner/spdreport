package cz.geek.spdreport.datastore

import com.google.cloud.datastore.DatastoreOptions
import com.googlecode.objectify.ObjectifyFactory
import com.googlecode.objectify.ObjectifyService
import cz.geek.spdreport.model.ObjectifyOAuth2AuthorizedClient
import cz.geek.spdreport.model.Settings
import cz.geek.spdreport.web.ObjectifyWebFilter
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.security.SecurityProperties
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

@Component
class ObjectifyConfiguration(
    @Value($$"${objectify.port:-1}")
    private val port: Int,
    @Value($$"${objectify.project: }")
    private val project: String,
) {

    @Bean
    fun filter() =
        FilterRegistrationBean(ObjectifyWebFilter())
            .also {
                init()
            }
            .apply {
                urlPatterns = setOf("/*")
                order = SecurityProperties.DEFAULT_FILTER_ORDER - 1
            }

    fun init() {
        initObjectifyService()
        ObjectifyService.register(ObjectifyOAuth2AuthorizedClient::class.java)
        ObjectifyService.register(Settings::class.java)
    }

    private fun initObjectifyService() {
        if (port == -1) {
            logger.info { "Initializing Objectify" }
            ObjectifyService.init()
        } else {
            logger.info { "Initializing Objectify on local port $port" }
            ObjectifyService.init(
                ObjectifyFactory(
                    DatastoreOptions.newBuilder()
                        .setHost("http://localhost:$port")
                        .setProjectId(project)
                        .build()
                        .service
                )
            )
        }
    }

}
