package me.devyonghee.accounttransactionjob

import org.springframework.batch.core.StepExecution
import org.springframework.batch.core.annotation.BeforeStep
import org.springframework.batch.item.ExecutionContext
import org.springframework.batch.item.ItemStreamReader
import org.springframework.batch.item.ParseException
import org.springframework.batch.item.file.transform.FieldSet

class TransactionReader(
    private val fieldSetReader: ItemStreamReader<FieldSet>,
) : ItemStreamReader<Transaction> {

    private lateinit var stepExecution: StepExecution
    private var recordCount = 0
    private var expectedRecordCount = 0

    override fun read(): Transaction? {
        // 오류 처리를 위한 조건문
        // 25번째 레코드를 읽은 후 ParseException 을 발생시킨다.
        if (recordCount == 25) {
            throw ParseException("this is not what i hoped to happen")
        }

        return process(fieldSetReader.read())
    }

    private fun process(fieldSet: FieldSet?): Transaction? {
        if (fieldSet == null) {
            return null;
        }

        // 레코드에 값이 하나만 있으면 푸터 레코드
        if (fieldSet.fieldCount <= 1) {
            expectedRecordCount = fieldSet.readInt(0)
            if (expectedRecordCount != recordCount) {
                stepExecution.setTerminateOnly()
            }
            return null
        }
        // 레코드에 값이 두 개 이상 존재하면 데이터 레코드
        return Transaction(
            accountNumber = fieldSet.readString(0),
            timestamp = fieldSet.readDate(1, "yyyy-MM-dd HH:mm:ss"),
            amount = fieldSet.readDouble(2),
        ).also {
            recordCount++
        }
    }

    // StepExecution 을 사용해 중지
    @BeforeStep
    fun beforeStep(stepExecution: StepExecution) {
        this.stepExecution = stepExecution
    }

    // 중지 트랜지션을 사용해 중지
    // @AfterStep
    // fun afterStep(execution: StepExecution): ExitStatus {
    //     if (recordCount == expectedRecordCount) {
    //         return execution.exitStatus
    //     }
    //     return ExitStatus.STOPPED
    // }

    override fun open(executionContext: ExecutionContext) {
        this.fieldSetReader.open(executionContext)
    }

    override fun update(executionContext: ExecutionContext) {
        this.fieldSetReader.update(executionContext)
    }

    override fun close() {
        this.fieldSetReader.close()
    }
}
