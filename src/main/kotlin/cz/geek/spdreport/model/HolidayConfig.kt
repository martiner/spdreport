package cz.geek.spdreport.model

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.net.URL
import java.util.*

data class CountryHolidayConfig(val calendar: URL, val matcher: String)

@Configuration
class HolidayConfig {

    @Bean
    @ConfigurationProperties(prefix = "holidays")
    fun holidayPropertiesMap(): Map<Country, CountryHolidayConfig> {
        return EnumMap(Country::class.java)
    }
}
