package cz.geek.spdreport

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.util.matcher.AntPathRequestMatcher

@Configuration
@EnableWebSecurity
class WebSecurityConfig {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http {
            authorizeHttpRequests {
                authorize("/", permitAll)
                authorize("/login/**", permitAll)
                authorize("/email/**", permitAll)
                authorize(anyRequest, authenticated)
            }
            logout {
                logoutSuccessUrl = "/"
                logoutRequestMatcher = AntPathRequestMatcher("/logout", "GET")
            }
            oauth2Login {
            }
        }
        return http.build()
    }
}
