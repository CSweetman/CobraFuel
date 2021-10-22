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

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)


val rooms = mutableMapOf<String, Room>()
val globalMutex = Mutex()

val a = englishWords.shuffled()

data class PlayerSession(val session: WebSocketSession, val playerID: Int)

class Room(
    val roomInfo: SerializableRoomInfo = SerializableRoomInfo(),
    val englishWordIterator: Iterator<String> = englishWords.shuffled().iterator(),
    var customWords: Iterator<String> = listOf<String>().iterator(),
    var profaneWords: Iterator<String> = listOf<String>().iterator(),
    val judgeRolesIterator: Iterator<String> = judgeRoles.shuffled().iterator(),
    val mutex: Mutex = Mutex(),
    val playerSessionToID: MutableList<PlayerSession> = mutableListOf()
)

val json = Json { explicitNulls = false; prettyPrint = false }

suspend fun WebSocketSession.sendMessage(message: Message) =
    this.send(json.encodeToString(Message.serializer(), message))


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

            globalMutex.withLock {
                if (!rooms.containsKey(roomCode))
                    rooms[roomCode as String] = Room()
                room = rooms[roomCode]!!
            }

            // Broadcasts a message to all users in the room
            suspend fun broadcast(msg: Message) {
                room.playerSessionToID.map {
                    it.session.sendMessage(msg)
                }
            }

            //
//            // Like above, but skips the client who prompted the server
            suspend fun broadcastSkipSender(msg: Message) {
                room.playerSessionToID.filter { it.session != this }.map {
                    it.session.sendMessage(msg)
                }
            }

            room.mutex.withLock {
                val newPlayerID = if (room.roomInfo.playerList.isEmpty()) 0 else room.roomInfo.playerList.last().id + 1
                val newPlayer = Player(id = newPlayerID)
                room.roomInfo.playerList += newPlayer
                room.playerSessionToID.add(PlayerSession(this, newPlayerID))
                sendMessage(Message(currentState = Message.CurrentState(room.roomInfo, newPlayerID)))
                broadcastSkipSender(Message(playerJoined = Message.PlayerJoined(newPlayer)))
            }
            val senderID = room.playerSessionToID.first { it.session == this }.playerID
            val sendingPlayer = room.roomInfo.playerList.first { it.id == senderID }
            try {
                while (true) {
                    val frame = incoming.receive()
                    if (frame is Frame.Text) {
                        room.mutex.withLock {
                            val message = json.decodeFromString(Message.serializer(), frame.readText())

                            if (message.setName != null) {
                                sendingPlayer.name = message.setName.name
                                broadcast(message)
                            }
                            else if (message.selectionOfRole != null) {
                                sendingPlayer.judgeRole = message.selectionOfRole.role
                                broadcastSkipSender(message)
                            }
                            else if (message.cardsPlayed != null) {
                                sendingPlayer.presentedHand = message.cardsPlayed.cards
                                broadcastSkipSender(message)
                            }
                            else if (message.endOfRoundRequest != null) {
                                // TODO implement
                            }
                        }
                        send(Frame.Text("Client said: " + frame.readText()))
                    }
                }
            } catch (e: Throwable) {
                globalMutex.withLock {
                    broadcastSkipSender(Message(playerLeft = Message.PlayerLeft(senderID)))
                    room.playerSessionToID.remove(room.playerSessionToID.first { it.playerID == senderID })
                    room.roomInfo.playerList.remove(sendingPlayer)
                    if (room.roomInfo.playerList.isEmpty())
                        rooms.remove(roomCode)
                    // TODO: If leaving player is judge, send end of round
                }
            }

            get("/json/gson") {
                call.respond(mapOf("hello" to "world"))
            }
        }
    }
}

