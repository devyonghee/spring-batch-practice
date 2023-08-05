package me.devyonghee.customertransactionitemreader.flatfile

import org.springframework.batch.item.file.transform.DefaultFieldSetFactory
import org.springframework.batch.item.file.transform.FieldSet
import org.springframework.batch.item.file.transform.FieldSetFactory
import org.springframework.batch.item.file.transform.LineTokenizer

object CustomerLineTokenizer : LineTokenizer {

    private const val DEFAULT_DELIMITER: String = ","
    private val FIELD_SET_FACTORY: FieldSetFactory = DefaultFieldSetFactory().apply { }
    private val NAMES: Array<String> = arrayOf(
        "firstName",
        "middleInitial",
        "lastName",
        "addressNumber",
        "street",
        "city",
        "state",
        "zipCode",
    )

    override fun tokenize(line: String?): FieldSet {
        if (line == null) {
            return FIELD_SET_FACTORY.create(arrayOf())
        }

        return line.split(DEFAULT_DELIMITER).let {
            FIELD_SET_FACTORY.create(it.toTypedArray(), NAMES)
        }
    }
}