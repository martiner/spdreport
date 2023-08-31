package cz.geek.spdreport

import com.ninjasquad.springmockk.MockkBean
import cz.geek.spdreport.TestHelper.oAuth2User
import io.kotest.assertions.assertSoftly
import io.kotest.core.spec.style.FreeSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.slot
import org.springframework.boot.info.GitProperties
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.security.oauth2.core.user.DefaultOAuth2User
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

@WebMvcTest
@ContextConfiguration(classes = [WebSecurityConfig::class, SettingsController::class])
class SettingsControllerTest(
    val context: WebApplicationContext,
    @MockkBean val settingsRepository: SettingsRepository,
    @MockkBean val gitProperties: GitProperties,
) : FreeSpec({

    lateinit var mockMvc: MockMvc

    beforeTest {
        mockMvc = MockMvcBuilders
            .webAppContextSetup(context)
            .apply<DefaultMockMvcBuilder>(springSecurity())
            .build()
    }

    "Should update Settings" {
        val settings = Settings(
            id = "123",
            email = "gi@example.com",
            fullName = "GI Joe",
            number = "789",
            url = "http://foo",
        )
        every { settingsRepository.load("123") } returns settings
        val slot = slot<Settings>()
        every { settingsRepository.save(capture(slot)) } just Runs

        mockMvc
            .post("/settings") {
                with(csrf())
                with(oauth2Login().oauth2User(oAuth2User(userId = "123")))
                param("id", "FORGED ID")
                param("email", "FORGED@EMA.IL")
                param("fullName", "GI Jane")
                param("number", "456")
                param("url", "http://bar")
            }
            .andExpect {
                status { is3xxRedirection() }
            }

        assertSoftly(slot.captured) {
            assertSoftly(id) {
                it shouldNotBe "FORGED ID"
                it shouldBe settings.id
            }
            assertSoftly(email) {
                it shouldNotBe "FORGED@EMA.IL"
                it shouldBe settings.email
            }
            fullName shouldBe "GI Jane"
            number shouldBe "456"
            url shouldBe "http://bar"
        }
    }
}) {
    override fun extensions() = listOf(SpringExtension)
}