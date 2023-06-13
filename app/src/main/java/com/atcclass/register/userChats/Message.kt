package com.atcclass.register.userChats

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class Message(var message: String? = null, var senderId: String? = null) {
    var timestamp: String
    var dateNTime: String = ""

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
