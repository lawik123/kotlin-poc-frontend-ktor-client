package nl.lawik.poc.frontend.ktor.client

import io.ktor.client.HttpClient
import io.ktor.client.features.BadResponseStatusException
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.request
import io.ktor.client.response.HttpResponse
import io.ktor.client.response.readText
import io.ktor.http.isSuccess
import kotlinx.io.core.use
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.list
import kotlinx.serialization.serializer

/**
 * Helper function for performing HTTP requests where the expected JSON body is a top-level array
 * which will be deserialized to a [List] of type [T].
 *
 * Request method is GET by default.
 */
suspend inline fun <reified T : Any> HttpClient.list(noinline block: HttpRequestBuilder.() -> Unit): List<T> =
    customSerializerRequest(T::class.serializer().list, block)

/**
 * Helper function for performing HTTP requests where its body will be deserialized using the provided [serializer].
 *
 * Request method is GET by default.
 */
suspend fun <T : Any> HttpClient.customSerializerRequest(
    serializer: KSerializer<T>,
    block: HttpRequestBuilder.() -> Unit
): T {
    return request<HttpResponse> {
        apply(block)
    }.use {
        if (it.status.isSuccess()) {
            return Json.parse(serializer, it.readText())
        } else {
            throw BadResponseStatusException(it.status, it)
        }
    }
}