package nl.lawik.poc.frontend.ktor.client

import io.ktor.client.HttpClient
import io.ktor.client.features.defaultRequest
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.request.*
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async

private const val API_HOST = "jsonplaceholder.typicode.com"
private const val API_PATH = "/todos"

val client = HttpClient {
    install(JsonFeature)
    defaultRequest {
        host = API_HOST
    }
}

fun main() {
    GlobalScope.async {
        println(getList())
        println(post())
        println(put())
        delete()
        println("Todo deleted")
    }
}

suspend fun getList(): List<Todo> = client.list {
    url(API_PATH)
}

suspend fun post(): Todo = client.post {
    url(API_PATH)
    body = Todo(1, "test", false)
    contentType(ContentType.Application.Json)
}

suspend fun put(): Todo = client.put {
    url("$API_PATH/1")
    body = Todo(1, "test2", true, 1)
    contentType(ContentType.Application.Json)
}

suspend fun delete() {
    client.delete<Unit> {
        url("$API_PATH/1")
    }
}
