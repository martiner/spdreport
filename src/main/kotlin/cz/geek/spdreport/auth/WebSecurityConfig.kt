package cz.geek.spdreport.auth

import cz.geek.spdreport.datastore.SettingsRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.util.matcher.AntPathRequestMatcher

@Configuration
@EnableWebSecurity
class WebSecurityConfig(
    private val repository: SettingsRepository,
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
                logoutRequestMatcher = AntPathRequestMatcher("/logout", "GET")
            }
            oauth2Login {
                authenticationSuccessHandler = smartAuthenticationSuccessHandler()
                userInfoEndpoint {
                    oidcUserService = googleUserService()
                }
            }
        }
        return http.build()
    }

    @Bean
    fun googleUserService() = GoogleUserService()
}