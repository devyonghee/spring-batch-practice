package me.devyonghee.apressbanking.transaction

import java.text.SimpleDateFormat
import java.util.Date
import javax.xml.bind.annotation.adapters.XmlAdapter

object JaxbDateSerializer : XmlAdapter<String, Date>() {

    private val DATE_FORMAT: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

    override fun unmarshal(v: String?): Date {
        return DATE_FORMAT.parse(v)
    }

    override fun marshal(v: Date?): String {
        return DATE_FORMAT.format(v)
    }
}

