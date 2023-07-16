package me.devyonghee.simplejobstep

import java.util.Date
import org.springframework.batch.core.JobParameters
import org.springframework.batch.core.JobParametersBuilder
import org.springframework.batch.core.JobParametersIncrementer

object DailyJobTimestamper : JobParametersIncrementer {

    override fun getNext(parameters: JobParameters?): JobParameters {
        return JobParametersBuilder(parameters ?: JobParameters())
            .addDate("currentDate", Date())
            .toJobParameters()
    }
}