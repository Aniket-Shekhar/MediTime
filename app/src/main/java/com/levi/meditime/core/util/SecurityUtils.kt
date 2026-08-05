package com.levi.meditime.core.util

object SecurityUtils {
    fun scrubPii(message: String?): String {
        if (message == null) return "Unknown error"
        // Simple regex to mask emails and phone numbers
        val emailRegex = "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}".toRegex()
        val phoneRegex = "\\+?[0-9]{10,15}".toRegex()
        
        return message
            .replace(emailRegex, "[EMAIL_MASKED]")
            .replace(phoneRegex, "[PHONE_MASKED]")
    }
}
