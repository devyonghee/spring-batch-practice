package me.devyonghee.runjoblauncher

import org.springframework.batch.core.ExitStatus
import org.springframework.batch.core.Job
import org.springframework.batch.core.JobParameter
import org.springframework.batch.core.JobParameters
import org.springframework.batch.core.JobParametersBuilder
import org.springframework.batch.core.explore.JobExplorer
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.context.ApplicationContext
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class JobController(
    private val context: ApplicationContext,
    private val jobLauncher: JobLauncher,
    private val jobExplorer: JobExplorer,
) {

    @PostMapping("/run")
    fun runJob(@RequestBody request: JobLauncherRequest): ResponseEntity<ExitStatus> {
        val job = context.getBean(request.name, Job::class.java)

        val parameters = JobParameters(request.jobProperties.mapValues { (_, value) ->
            JobParameter(value, value.javaClass)
        })
        val jobParameters: JobParameters = JobParametersBuilder(parameters, jobExplorer)
            .getNextJobParameters(job)
            .toJobParameters()

        return ResponseEntity.ok(
            jobLauncher.run(job, jobParameters).exitStatus
        )
    }

    data class JobLauncherRequest(
        val name: String,
        val jobProperties: Map<String, Any>,
    )
}