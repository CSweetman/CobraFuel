package io.github.JMoore34_CSweetman

import io.github.JMoore34_CSweetman.SerializableClasses.SerializableRoomInfo
import io.ktor.application.*
import io.ktor.response.*
import io.ktor.features.*
import io.ktor.routing.*
import io.ktor.http.*
import io.ktor.websocket.*
import io.ktor.http.cio.websocket.*
import java.time.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.serialization.json.*

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

val rooms = mutableMapOf<String, SerializableRoomInfo>()
val globalMutex = Mutex()





/*class Message(
    //Client to Server
    val room: Room?
    val playerID:
)*/

//val jackson = Jackson

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {
    install(CORS) {
        method(HttpMethod.Options)
        method(HttpMethod.Put)
        method(HttpMethod.Delete)
        method(HttpMethod.Patch)
        header(HttpHeaders.Authorization)
        header("MyCustomHeader")
        allowCredentials = true
        anyHost() // @TODO: Don't do this in production if possible. Try to limit it.
    }

    install(io.ktor.websocket.WebSockets) {
        pingPeriod = Duration.ofSeconds(15)
        timeout = Duration.ofSeconds(15)
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }

    routing {
        get("/") {
            call.respondText("HELLO WORLD!", contentType = ContentType.Text.Plain)
        }

        webSocket("/myws/echo") {
            send(Frame.Text("Hi from server"))
            while (true) {
                val frame = incoming.receive()
                if (frame is Frame.Text) {
                    send(Frame.Text("Client said: " + frame.readText()))
                }
            }
        }

        get("/json/gson") {
            call.respond(mapOf("hello" to "world"))
        }
    }
}

