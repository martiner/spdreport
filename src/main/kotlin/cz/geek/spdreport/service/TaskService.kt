package cz.geek.spdreport.service

import com.google.cloud.tasks.v2.AppEngineHttpRequest
import com.google.cloud.tasks.v2.CloudTasksClient
import com.google.cloud.tasks.v2.HttpMethod.GET
import com.google.cloud.tasks.v2.QueueName
import com.google.cloud.tasks.v2.Task
import cz.geek.spdreport.model.EmailFrequency
import cz.geek.spdreport.model.EmailFrequency.NONE
import cz.geek.spdreport.model.Settings
import cz.geek.spdreport.datastore.SettingsRepository
import mu.KotlinLogging
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger {}

@Service
class TaskService(
    private val settingsRepository: SettingsRepository,
) {

    private val queue = QueueName.of("spdreport", "europe-west3", "emails")

    fun scheduleReports(freq: EmailFrequency) {
        require(freq != NONE) { "NONE" }
        logger.info { "Scheduling reports with $freq frequency" }
        settingsRepository.find(freq)
            .forEach(this::scheduleReport)
    }

    fun scheduleReport(user: String) {
        settingsRepository.load(user)
            ?.let(this::scheduleReport)
            ?: logger.warn { "No settings found for $user" }
    }

    private fun scheduleReport(settings: Settings) {
        CloudTasksClient
            .create()
            .use { client ->
                settings.toTask()
                    .let { client.createTask(queue, it) }
                    .also { logger.info { "Scheduled report for user ${settings.id} task ${it.name}" } }
            }
    }

    private fun Settings.toTask(): Task =
        Task.newBuilder()
            .setAppEngineHttpRequest(
                AppEngineHttpRequest.newBuilder()
                    .setRelativeUri("/email/settings/$id")
                    .setHttpMethod(GET)
                    .build()
            )
            .build()
}
