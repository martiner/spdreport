package cz.geek.spdreport.web

import cz.geek.spdreport.datastore.SettingsRepository
import cz.geek.spdreport.model.DateRanges
import cz.geek.spdreport.model.EmailFrequency
import cz.geek.spdreport.model.Settings.Companion.toSettingsData
import cz.geek.spdreport.model.SettingsData
import cz.geek.spdreport.web.SettingsController.Companion.URL
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.servlet.mvc.support.RedirectAttributes

@Controller
@RequestMapping(URL)
class SettingsController(
    private val settingsRepository: SettingsRepository,
) {

    @GetMapping
    fun get() = FORM

    @PostMapping
    fun post(@ModelAttribute(MODEL) settings: SettingsData, errors: BindingResult,
             @AuthenticationPrincipal principal: OAuth2AuthenticatedPrincipal,
             redirectAttributes: RedirectAttributes): String {
        if (errors.hasErrors()) {
            return FORM
        }
        settingsRepository.save(settings.toSettings(principal))
        redirectAttributes.addFlashAttribute("message", "Saved")
        return "redirect:$URL"
    }

    @ModelAttribute
    fun model(@AuthenticationPrincipal principal: OAuth2AuthenticatedPrincipal, model: Model) {
        val settings = settingsRepository.load(principal)?.toSettingsData() ?: SettingsData(principal)
        model.addAttribute(MODEL, settings)
        model.addAttribute("ranges", DateRanges.entries)
        model.addAttribute("freq", EmailFrequency.entries)
    }
    
    companion object {
        const val URL = "/settings"
        private const val FORM = "settings"
        private const val MODEL = "settings"
    }
}
