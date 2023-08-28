package me.devyonghee.cloudnativebatch

import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.batch.core.JobExecution
import org.springframework.batch.core.JobExecutionListener
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
import org.springframework.core.io.support.ResourcePatternResolver
import org.springframework.util.StreamUtils

class DownloadingJobExecutionListener(
    private val resourcePatternResolver: ResourcePatternResolver,
    @Value("\${job.resource - path}") private val path: String,
) : JobExecutionListener {

    private val log: Logger = LoggerFactory.getLogger(javaClass)

    override fun beforeJob(jobExecution: JobExecution) {
        try {
            jobExecution.executionContext.put("localFiles", filesAbsolutePaths())
        } catch (e: IOException) {
            log.error("Failed to download files", e)
        }
    }

    private fun filesAbsolutePaths(): String {
        return resourcePatternResolver.getResources(path)
            .joinToString {
                copyResourceToFile(it).absolutePath
            }
    }

    private fun copyResourceToFile(it: Resource): File {
        return File.createTempFile("input", ".csv").apply {
            StreamUtils.copy(it.inputStream, FileOutputStream(this))
            log.info("Downloaded file: {}", absolutePath)
        }
    }
}
