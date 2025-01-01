package cz.geek.spdreport

import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.WebDataBinder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.InitBinder
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import java.net.URL

private const val URL = "/settings"
private const val FORM = "settings"
private const val MODEL = "settings"

@Controller
@RequestMapping(URL)
class SettingsController(
    private val settingsRepository: SettingsRepository,
) {

    @GetMapping
    fun get() = FORM

    @PostMapping
    fun post(@ModelAttribute(MODEL) settings: Settings, redirectAttributes: RedirectAttributes): String {
        settingsRepository.save(settings)
        redirectAttributes.addFlashAttribute("message", "Saved")
        return "redirect:$URL"
    }

    @ModelAttribute
    fun model(@AuthenticationPrincipal principal: OAuth2AuthenticatedPrincipal, model: Model) {
        val settings = settingsRepository.load(principal.name) ?: Settings(principal)
        model.addAttribute(MODEL, settings)
        model.addAttribute("ranges", DateRanges.values())
        model.addAttribute("freq", EmailFrequency.values())
    }
    
    @InitBinder
    fun binder(binder: WebDataBinder) {
        binder.setDisallowedFields("id", "email")
    }

}
