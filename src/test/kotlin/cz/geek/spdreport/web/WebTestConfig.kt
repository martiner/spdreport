package cz.geek.spdreport.web

import cz.geek.spdreport.auth.WebSecurityConfig
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Import

@TestConfiguration
@Import(WebSecurityConfig::class, ControllerModel::class)
class WebTestConfig
