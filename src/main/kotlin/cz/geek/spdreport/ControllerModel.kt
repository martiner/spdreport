package cz.geek.spdreport

import org.springframework.boot.info.GitProperties
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ModelAttribute

@ControllerAdvice
class ControllerModel(
    private val gitProperties: GitProperties,
) {

    @ModelAttribute
    fun model(model: Model) {
        model.addAttribute("git", gitProperties)
    }
}
