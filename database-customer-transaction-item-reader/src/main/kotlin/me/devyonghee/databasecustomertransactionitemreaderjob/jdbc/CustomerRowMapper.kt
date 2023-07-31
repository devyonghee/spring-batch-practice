package me.devyonghee.databasecustomertransactionitemreaderjob.jdbc

import java.sql.ResultSet
import org.springframework.jdbc.core.RowMapper

object CustomerRowMapper : RowMapper<Customer> {

    override fun mapRow(rs: ResultSet, rowNum: Int): Customer {
        return Customer(
            rs.getLong("id"),
            rs.getString("first_name"),
            rs.getString("middle_initial"),
            rs.getString("last_name"),
            rs.getString("address"),
            rs.getString("city"),
            rs.getString("state"),
            rs.getString("zip_code"),
        )
    }
}