package com.atcclass.register.groupChats

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class GroupMessage(
    var senderId: String? = null,
    var message: String,
    var timestamp: Any? = null,
    var our_members: ArrayList<String>? = null
) {
    var dateNTime: String = ""

    init {
        // Get the current date and time
        val currentDateTime = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")

        // Format the date and time and set it to the dateNTime variable
        dateNTime = currentDateTime.format(formatter)

        // Set the formatted date and time to the timestamp property if it is null
        if (timestamp == null) {
            timestamp = currentDateTime.format(formatter)
        }
    }

    // Empty constructor required by Firebase
    constructor() : this(message = "")
}
