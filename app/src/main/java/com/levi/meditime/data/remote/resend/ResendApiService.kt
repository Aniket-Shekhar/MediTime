package com.levi.meditime.data.remote.resend

import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface ResendApiService {
    @POST("emails")
    suspend fun sendEmail(
        @Header("Authorization") apiKey: String,
        @Body request: ResendEmailRequest
    ): ResendEmailResponse
}

data class ResendEmailRequest(
    val from: String,
    val to: List<String>,
    val subject: String,
    val html: String
)

data class ResendEmailResponse(
    val id: String
)
