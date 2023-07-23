package me.devyonghee.runjoblauncher

import org.quartz.JobBuilder
import org.quartz.JobDetail
import org.quartz.ScheduleBuilder
import org.quartz.SimpleScheduleBuilder
import org.quartz.SimpleTrigger
import org.quartz.Trigger
import org.quartz.TriggerBuilder
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class QuartzConfiguration() {
    private val log: Logger = LoggerFactory.getLogger(javaClass)

    @Bean
    fun quartzJobDetail(): JobDetail {
        return JobBuilder.newJob(QuartzBatchScheduledJob::class.java)
            .storeDurably()
            .build()
    }

    @Bean
    fun jobTrigger(): Trigger {
        val scheduleBuilder: ScheduleBuilder<SimpleTrigger> = SimpleScheduleBuilder.simpleSchedule()
            .withIntervalInSeconds(5)
            .withRepeatCount(4)

        return TriggerBuilder.newTrigger()
            .forJob(quartzJobDetail())
            .withSchedule(scheduleBuilder)
            .build()
    }
}