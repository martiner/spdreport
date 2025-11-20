package cz.geek.spdreport.web

import com.ninjasquad.springmockk.MockkBean
import cz.geek.spdreport.auth.WebSecurityConfig
import cz.geek.spdreport.datastore.SettingsRepository
import org.springframework.boot.info.GitProperties
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Import

@TestConfiguration
@Import(WebSecurityConfig::class, ControllerModel::class)
class WebTestConfig(
    @MockkBean val settingsRepository: SettingsRepository,
    @MockkBean(relaxed = true) val gitProperties: GitProperties,
) {
}