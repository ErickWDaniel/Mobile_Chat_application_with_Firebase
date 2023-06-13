package com.atcclass.register.UserChats

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class GroupMessage(var message: String? = null, var senderId: String? = null) {
    val timestamp: String
    var dateNTime: String = ""
    var groupMember: ArrayList<String>? = null

    init {
        // Get the current date and time
        val currentDateTime = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")

        // Format the date and time and set it to the dateNTime variable
        dateNTime = currentDateTime.format(formatter)

        // Set the formatted date and time to the timestamp property
        timestamp = dateNTime
    }

}
