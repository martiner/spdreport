package cz.geek.spdreport.web

import com.ninjasquad.springmockk.MockkBean
import cz.geek.spdreport.auth.PagerDutyUser
import cz.geek.spdreport.datastore.SettingsRepository
import cz.geek.spdreport.service.ReportService
import io.kotest.assertions.assertSoftly
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.validation.BindingResult
import org.springframework.web.context.WebApplicationContext

@WebMvcTest
@ContextConfiguration(classes = [WebTestConfig::class, ReportController::class])
class ReportControllerTest(
    val context: WebApplicationContext,
    val settingsRepository: SettingsRepository,
    @MockkBean val service: ReportService,
) : FreeSpec({

    lateinit var mockMvc: MockMvc

    beforeTest {
        mockMvc = MockMvcBuilders
            .webAppContextSetup(context)
            .apply<DefaultMockMvcBuilder>(springSecurity())
            .build()
    }

    "Should fail with errors" {
        mockMvc
            .post("/report") {
                with(csrf())
                param("url", "foo")
            }
            .andExpect {
                model {
                    attributeHasFieldErrorCode("reportData", "url", "typeMismatch")
                }
            }
    }

    "Should fail for iCal with PagerDuty login" {
        every { settingsRepository.load(any<OAuth2AuthenticatedPrincipal>()) } returns null // just dummy response
        mockMvc
            .post("/report") {
                with(csrf())
                with(oauth2Login().oauth2User(PagerDutyUser(mapOf("id" to "123"))))
                param("url", "http://foo")
            }
            .andReturn()
            .apply {
                assertSoftly(modelAndView) {
                    shouldNotBeNull()
                    assertSoftly(model) {
                        shouldNotBeNull()
                        assertSoftly(get(BindingResult.MODEL_KEY_PREFIX + "reportData")) {
                            shouldNotBeNull()
                            shouldBeInstanceOf<BindingResult>()
                            assertSoftly(globalError) {
                                shouldNotBeNull()
                                assertSoftly(codes) {
                                    shouldNotBeNull()
                                    shouldContain("pdical")
                                }
                            }
                        }
                    }
                }
            }
    }
})
