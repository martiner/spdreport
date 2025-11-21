package cz.geek.spdreport.web

import cz.geek.spdreport.auth.User
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/")
class IndexController {

    @GetMapping
    fun get(@AuthenticationPrincipal principal: User?): String =
        if (principal != null) "redirect:${ReportController.URL}" else "index"
}