package me.devyonghee.customeritemwriter.flatfile

import java.io.Writer
import org.aspectj.lang.JoinPoint
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Before
import org.springframework.batch.item.Chunk
import org.springframework.batch.item.file.FlatFileFooterCallback
import org.springframework.stereotype.Component

@Aspect
@Component
class CustomerRecordCountFooterCallback : FlatFileFooterCallback {

    private var itemsWrittenInCurrentFile: Int = 0

    override fun writeFooter(writer: Writer) {
        writer.write("this file contains $itemsWrittenInCurrentFile items")
    }

    @Before("execution(* org.springframework.batch.item.support.AbstractFileItemWriter.open(..))")
    fun resetCount() {
        itemsWrittenInCurrentFile = 0
    }

    @Before("execution(* org.springframework.batch.item.support.AbstractFileItemWriter.write(..))")
    fun beforeWriter(joinPoint: JoinPoint) {
        val argument: Any = joinPoint.args[0]
        if (argument is Chunk<*>) {
            itemsWrittenInCurrentFile += argument.items.size
        }
    }
}