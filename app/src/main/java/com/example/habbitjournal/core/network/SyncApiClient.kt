package com.example.habbitjournal.core.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class SyncLogDto(
    @SerialName("record_date") val recordDate: String,
    val count: Int,
    @SerialName("updated_at") val updatedAt: String,
)

@Serializable
data class SyncPushRequest(val logs: List<SyncLogDto>)

@Serializable
data class SyncPullResponse(val logs: List<SyncLogDto>, @SerialName("server_time") val serverTime: String)

@Singleton
class SyncApiClient @Inject constructor() {
    private val client = OkHttpClient()
    private val json = Json { ignoreUnknownKeys = true }

    fun push(serverUrl: String, logs: List<SyncLogDto>) {
        val body = json.encodeToString(SyncPushRequest.serializer(), SyncPushRequest(logs))
            .toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url("$serverUrl/api/v1/sync/push")
            .post(body)
            .build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("push failed: ${response.code}")
            }
        }
    }

    fun pull(serverUrl: String, since: String): SyncPullResponse {
        val request = Request.Builder()
            .url("$serverUrl/api/v1/sync/pull?since=$since")
            .get()
            .build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("pull failed: ${response.code}")
            }
            val body = response.body?.string() ?: throw IOException("empty body")
            return json.decodeFromString(SyncPullResponse.serializer(), body)
        }
    }
}
