# Kotlin Ktor client PoC
This project showcases the usage of the Ktor (JavaScript) client.

## Installation
1. Clone this repository
2. Run the following command in the root directory of the project `gradlew -t run` this will start a webpack dev server in continuous mode (code changes will refresh the browser automatically).

## About
The Ktor client allows you to make HTTP requests, the Ktor ktor-client-json library (in conjunction with kotlinx.serialization) allows for automatic (de)serialization.
To make this possible you must first create the client and install the JsonFeature (features are like filter/interceptors):
 ```kotlin
private const val API_HOST = "jsonplaceholder.typicode.com"
 
val client = HttpClient {
    install(JsonFeature)
    defaultRequest {
        host = API_HOST
    }
}
```
Now you can use the client to make HTTP requests. As you can see a default host has also been provided, now you don't need to provide the host in individual requests but only the API path.

Below is an example of a POST (the client has support for all HTTP request methods) request.
```kotlin
suspend fun post(): Todo = client.post {
    url(API_PATH)
    body = Todo(1, "test", false)
    contentType(ContentType.Application.Json)
}
```
As you can see there is no need to manually (de)serialize, the request body will be serialized automatically because the content-type is application/json.
And the response body will be deserialized automatically based on the function's return type.

NOTE: You must annotate your class (in this example Todo) with kotlinx.serialization's `@Serializable` annotation in order for this to work!

As you can see the function above is a suspend function which means it makes use of kotlin coroutines, so there is no need to use .then()

The library will not automatically deserialize a top-level JSON list as it doesnt acknowledge this as valid JSON, the problem is that a lot of APIs do return a top-level JSON list.
I have created a workaround which allows this:
```kotlin
suspend fun getList(): List<Todo> = client.list {
    url(API_PATH)
}
```
The function above sends a GET request, you can use any HTTP method by setting the `method` value (along with any other Ktor request configurations):
```kotlin
suspend fun getList(): List<Todo> = client.list {
    url(API_PATH)
    method = HttpMethod.Post
}
```

The `list` method looks as follows:
```kotlin
suspend inline fun <reified T : Any> HttpClient.list(noinline block: HttpRequestBuilder.() -> Unit): List<T> =
    customSerializerRequest(T::class.serializer().list, block)
```
As you can see it calls the `customSerializerRequest` passing the `list` serializer of the class and the`HtttpRequestBuilder` configuration.

The `customSerializerRequest` looks as follows:
 ```kotlin
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
```
This function accept a serializer and the request configuration.
It will perform a request "manually" using the request configuration and then check whether the request was successful, if so it will parse the body using the provided serializer, if not it will throw a `BadResponseStatusException` (which is the default behaviour for Ktor).