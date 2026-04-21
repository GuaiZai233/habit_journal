package com.example.habbitjournal.core.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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

@Serializable
data class HealthResponse(val status: String)

@Singleton
class SyncApiClient @Inject constructor() {
    private val client = OkHttpClient()
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun checkHealth(serverUrl: String): HealthResponse = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("$serverUrl/api/v1/health")
            .get()
            .build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("health check failed: ${response.code}")
            }
            val body = response.body?.string() ?: throw IOException("empty body")
            val parsed = json.decodeFromString(HealthResponse.serializer(), body)
            if (parsed.status.lowercase() != "ok") {
                throw IOException("health status is not ok")
            }
            parsed
        }
    }

    suspend fun push(serverUrl: String, logs: List<SyncLogDto>) = withContext(Dispatchers.IO) {
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

    suspend fun pull(serverUrl: String, since: String): SyncPullResponse = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("$serverUrl/api/v1/sync/pull?since=$since")
            .get()
            .build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("pull failed: ${response.code}")
            }
            val body = response.body?.string() ?: throw IOException("empty body")
            json.decodeFromString(SyncPullResponse.serializer(), body)
        }
    }
}