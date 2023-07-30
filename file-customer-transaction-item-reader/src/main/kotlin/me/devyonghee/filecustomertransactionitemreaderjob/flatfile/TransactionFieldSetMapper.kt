package me.devyonghee.filecustomertransactionitemreaderjob.flatfile

import org.springframework.batch.item.file.mapping.FieldSetMapper
import org.springframework.batch.item.file.transform.FieldSet

object TransactionFieldSetMapper : FieldSetMapper<CustomerLineType> {

    override fun mapFieldSet(fieldSet: FieldSet): Transaction {
        return Transaction(
            fieldSet.readString("accountNumber"),
            fieldSet.readDate("transactionDate"),
            fieldSet.readDouble("amount"),
        )
    }
}