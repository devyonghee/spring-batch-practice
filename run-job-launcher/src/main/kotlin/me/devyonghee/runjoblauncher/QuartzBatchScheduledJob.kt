package me.devyonghee.runjoblauncher

import org.quartz.JobExecutionContext
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.batch.core.Job
import org.springframework.batch.core.JobParameters
import org.springframework.batch.core.JobParametersBuilder
import org.springframework.batch.core.explore.JobExplorer
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.scheduling.quartz.QuartzJobBean

class QuartzBatchScheduledJob(
    @Qualifier("quartzJob") private val job: Job,
    private val jobExplorer: JobExplorer,
    private val jobLauncher: JobLauncher,
) : QuartzJobBean() {

    private val log: Logger = LoggerFactory.getLogger(javaClass);

    override fun executeInternal(context: JobExecutionContext) {
        val jobParameters: JobParameters = JobParametersBuilder(jobExplorer)
            .getNextJobParameters(job)
            .toJobParameters()

        try {
            jobLauncher.run(job, jobParameters)
        } catch (e: Exception) {
            log.error("Job failed", e)
        }
    }
}