package cz.geek.spdreport.auth

import cz.geek.spdreport.datastore.SettingsRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher

@Configuration
@EnableWebSecurity
class WebSecurityConfig(
    private val repository: SettingsRepository,
    private val pagerDutyUserService: PagerDutyUserService,
) {

    @Bean
    fun smartAuthenticationSuccessHandler() =
        SmartAuthenticationSuccessHandler(repository)

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http {
            authorizeHttpRequests {
                authorize("/settings/**", authenticated)
                authorize(anyRequest, permitAll)
            }
            logout {
                logoutSuccessUrl = "/"
                logoutRequestMatcher = PathPatternRequestMatcher.withDefaults().matcher(HttpMethod.GET, "/logout")
            }
            oauth2Login {
                authenticationSuccessHandler = smartAuthenticationSuccessHandler()
                userInfoEndpoint {
                    userService = pagerDutyUserService
                    oidcUserService = googleUserService()
                }
            }
        }
        return http.build()
    }

    @Bean
    fun googleUserService() = GoogleUserService()
}