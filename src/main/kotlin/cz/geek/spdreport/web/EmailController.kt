package cz.geek.spdreport.web

import cz.geek.spdreport.model.EmailFrequency.MONTHLY
import cz.geek.spdreport.model.EmailFrequency.WEEKLY
import cz.geek.spdreport.service.EmailService
import cz.geek.spdreport.service.TaskService
import mu.KotlinLogging
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody

private const val CRON_HEADER = "X-AppEngine-Cron"
private const val TASK_HEADER = "X-AppEngine-TaskName"

private val logger = KotlinLogging.logger {}

@Controller
@RequestMapping("/email")
class EmailController(
    private val emailService: EmailService,
    private val taskService: TaskService,
) {

    @GetMapping(path = ["weekly"], headers = [CRON_HEADER])
    @ResponseBody
    fun weekly(): String {
        taskService.scheduleReports(WEEKLY)
        return "OK"
    }

    @GetMapping("monthly", headers = [CRON_HEADER])
    @ResponseBody
    fun monthly(): String {
        taskService.scheduleReports(MONTHLY)
        return "OK"
    }

    @GetMapping(path = ["settings/{user}"], headers = [TASK_HEADER])
    @ResponseBody
    fun settings(@PathVariable user: String, @RequestHeader(TASK_HEADER) taskName: String): String {
        logger.info { "Sending report task $taskName for $user" }
        emailService.sendReport(user)
        return "OK"
    }

    @GetMapping("self")
    @ResponseBody
    fun self(@AuthenticationPrincipal principal: OAuth2AuthenticatedPrincipal): String {
        val user = principal.name
        logger.info { "Sending report for $user" }
        emailService.sendReport(user)
        return "OK"
    }

    @GetMapping("selftask")
    @ResponseBody
    fun selfTask(@AuthenticationPrincipal principal: OAuth2AuthenticatedPrincipal): String {
        val user = principal.name
        logger.info { "Scheduling report for $user" }
        taskService.scheduleReport(user)
        return "OK"
    }
}
