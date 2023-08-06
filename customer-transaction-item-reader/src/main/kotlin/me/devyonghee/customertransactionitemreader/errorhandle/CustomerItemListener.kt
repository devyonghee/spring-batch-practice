package me.devyonghee.customertransactionitemreader.errorhandle

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.batch.core.annotation.OnReadError
import org.springframework.batch.item.file.FlatFileParseException


object CustomerItemListener {

    private val log: Logger = LoggerFactory.getLogger(javaClass)

    @OnReadError
    fun onReadError(ex: Exception) {
        if (ex !is FlatFileParseException) {
            log.error("an error has occurred", ex)
            return
        }

        log.error(
            """
            An error occured while processing the ${ex.lineNumber} line of the file. Below was the faulty input. 
            ${ex.input}
        """.trimIndent(), ex
        )
    }
}