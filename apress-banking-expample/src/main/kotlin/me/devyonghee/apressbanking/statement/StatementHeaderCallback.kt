package me.devyonghee.apressbanking.statement

import java.io.Writer
import org.springframework.batch.item.file.FlatFileHeaderCallback

object StatementHeaderCallback : FlatFileHeaderCallback {

    override fun writeHeader(writer: Writer) {
        writer.write(
            """
            ${"Customer Service Number".padStart(120)}
            ${"(800) 867-5309".padStart(120)}
            ${"Available 24/7".padStart(120)}
            
            """.trimIndent()
        )
    }
}