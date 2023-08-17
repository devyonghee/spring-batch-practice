package me.devyonghee.customeritemwriter.mongodb

import java.io.Serializable
import org.bson.types.ObjectId
import org.springframework.data.annotation.Id

data class CustomerMongo(
    @Id
    val id: ObjectId = ObjectId.get(),
    val firstName: String,
    val middleInitial: String,
    val lastName: String,
    val address: String,
    val city: String,
    val state: String,
    val zipCode: String,
    val email: String,
) : Serializable {

    companion object {
        @Suppress("ConstPropertyName")
        private const val serialVersionUID = 1L
    }
}