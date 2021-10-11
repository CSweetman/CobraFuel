package io.github.JMoore34_CSweetman

import io.github.JMoore34_CSweetman.SerializableClasses.Message
import io.github.JMoore34_CSweetman.SerializableClasses.Player
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
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.*
import java.io.File

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)


val rooms = mutableMapOf<String, Room>()
val globalMutex = Mutex()

val a = englishWords.shuffled()

class Room(val roomInfo: SerializableRoomInfo = SerializableRoomInfo(),
           val englishWordIterator: Iterator<String> = englishWords.shuffled().iterator(),
           var customWords: Iterator<String> = listOf<String>().iterator(),
           var profaneWords: Iterator<String> = listOf<String>().iterator(),
           val judgeRolesIterator: Iterator<String> = judgeRoles.shuffled().iterator(),
           val mutex: Mutex = Mutex(),
)

val json = Json { explicitNulls = false; prettyPrint = false }

suspend fun WebSocketSession.sendMessage(message: Message) = this.send(json.encodeToString(Message.serializer(), message))

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

        webSocket("/{roomCode}") {
            send(Frame.Text("Hi from server"))

            val roomCode = call.parameters["roomCode"]
            if (roomCode.isNullOrBlank())
                send("Error: Must specify a room code")

            var room: Room

            globalMutex.withLock{
                if(!rooms.containsKey(roomCode))
                    rooms[roomCode as String] = Room()
                room = rooms[roomCode]!!

                room.roomInfo.playerList += Player(id = room.roomInfo.playerList.size)
            }

            // Broadcasts a message to all users in the room
//            suspend fun broadcast(msg: Message) {
//                room.roomInfo.players.keys.map {
//                    it.sendMessage(msg)
//                }
//            }
//
//            // Like above, but skips the client who prompted the server
//            suspend fun broadcastSkipSender(msg: Message) {
//                room.users.keys.filter { it != this}.map {
//                    it.sendMessage(msg)
//                }
//            }

            room.mutex.withLock {}


                // Inform the new user of the existing roles and users
//                sendMessage(Message(roles = room.roles.toList()))
//                sendMessage(Message(users = room.users.values.toList()))

                // Inform everyone else of the update to the user list because this new user joined


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

