package com.example.dubstage.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import okhttp3.MultipartBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface DemucsApiService {
    @Multipart
    @POST("200")
    suspend fun separateAudio(
        @Part audio: MultipartBody.Part
    ): DemucsBackendResponse
}

@JsonClass(generateAdapter = true)
data class DemucsBackendResponse(
    @Json(name = "vocals_url") val vocalsUrl: String?,
    @Json(name = "backing_url") val backingUrl: String?,
    @Json(name = "error") val error: String? = null
)
