package cz.geek.spdreport.web

import com.ninjasquad.springmockk.MockkBean
import cz.geek.spdreport.service.ReportService
import io.kotest.core.spec.style.FreeSpec
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

@WebMvcTest
@ContextConfiguration(classes = [WebTestConfig::class, ReportController::class])
class ReportControllerTest(
    val context: WebApplicationContext,
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
            .post("/") {
                with(csrf())
                param("url", "foo")
            }
            .andExpect {
                model {
                    attributeHasFieldErrorCode("reportData", "url", "typeMismatch")
                }
            }
    }
})
