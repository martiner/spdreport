package cz.geek.spdreport

import com.ninjasquad.springmockk.MockkBean
import cz.geek.spdreport.EmailFrequency.MONTHLY
import cz.geek.spdreport.EmailFrequency.WEEKLY
import cz.geek.spdreport.TestHelper.oAuth2User
import io.kotest.core.spec.style.FreeSpec
import io.kotest.datatest.withData
import io.kotest.extensions.spring.SpringExtension
import io.mockk.confirmVerified
import io.mockk.verify
import org.springframework.boot.info.GitProperties
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

@WebMvcTest
@ContextConfiguration(classes = [WebSecurityConfig::class, EmailController::class])
class EmailControllerTest(
    private val context: WebApplicationContext,
    @MockkBean(relaxed = true) val emailService: EmailService,
    @MockkBean(relaxed = true) val taskService: TaskService,
    @MockkBean val gitProperties: GitProperties,
) : FreeSpec({

    lateinit var mockMvc: MockMvc

    beforeTest {
        mockMvc = MockMvcBuilders
            .webAppContextSetup(context)
            .apply<DefaultMockMvcBuilder>(springSecurity())
            .build()
    }

    "Should schedule report" - {
        withData(
            "weekly" to WEEKLY,
            "monthly" to MONTHLY,
        ) { (path, frequency) ->
            mockMvc
                .get("/email/$path") {
                    header("X-AppEngine-Cron", "true")
                }
                .andExpect { status { isOk() } }
            verify { taskService.scheduleReports(frequency) }
        }
    }

    "Should process user report" {
        mockMvc
            .get("/email/settings/user") {
                header("X-AppEngine-TaskName", "task")
            }
            .andExpect { status { isOk() } }
        verify { emailService.sendReport("user") }
    }

    "Should send self report" {
        mockMvc
            .get("/email/self") {
                with(csrf())
                with(oauth2Login().oauth2User(oAuth2User(userId = "123")))
            }
            .andExpect { status { isOk() } }
        verify { emailService.sendReport("123") }
    }

    "Should schedule self report" {
        mockMvc
            .get("/email/selftask") {
                with(csrf())
                with(oauth2Login().oauth2User(oAuth2User(userId = "123")))
            }
            .andExpect { status { isOk() } }
        verify { taskService.scheduleReport("123") }
    }

    "Should skip execution without headers" - {
        withData(
            "weekly",
            "monthly",
            "settings/foo",
        ) { path ->
            mockMvc
                .get("/email/$path")
                .andExpect { status { isNotFound() } }
            confirmVerified(taskService, emailService)
        }
    }
}) {
    override fun extensions() = listOf(SpringExtension)
}
