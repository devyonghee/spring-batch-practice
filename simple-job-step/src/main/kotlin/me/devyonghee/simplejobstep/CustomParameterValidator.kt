package me.devyonghee.simplejobstep

import org.springframework.batch.core.JobParameters
import org.springframework.batch.core.JobParametersInvalidException
import org.springframework.batch.core.JobParametersValidator

object CustomParameterValidator : JobParametersValidator {

    override fun validate(parameters: JobParameters?) {
        val fileName: String? = parameters?.getString("fileName")
        if (fileName.isNullOrBlank()) {
            throw JobParametersInvalidException("file name must be provided")
        }

        if (!fileName.endsWith("csv", ignoreCase = false)) {
            throw JobParametersInvalidException("file name parameter does not use the csv file extension. fileName(`${fileName}`)")
        }
    }
}