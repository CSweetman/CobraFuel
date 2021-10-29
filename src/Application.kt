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



class Room(
    val roomInfo: SerializableRoomInfo = SerializableRoomInfo(),
    val words: Sequence<String> = sequence {
        while (true)
            yieldAll(englishWords.shuffled())
    },
    val judgeRoleSequence: Sequence<String> = sequence {
        while (true)
            yieldAll(judgeRoles.shuffled())
    },
    val mutex: Mutex = Mutex(),
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
                room.roomInfo.playerList.map {
                    it.session!!.sendMessage(msg)
                }
            }

            //
//            // Like above, but skips the client who prompted the server
            suspend fun broadcastSkipSender(msg: Message) {
                room.roomInfo.playerList.filter { it.session != this }.map {
                    it.session!!.sendMessage(msg)
                }
            }

            suspend fun callEndOfRound(winnerPlayerID: Int?) {
                var winner: Player? = null
                if (winnerPlayerID != null) {
                    winner = room.roomInfo.playerList.first { it.id == winnerPlayerID }
                    winner.rolesWon += room.roomInfo.judgeRole
                }
                val currentJudgeIndex =
                    room.roomInfo.playerList.indexOfFirst { it.id == room.roomInfo.judgePlayerID }
                val newJudgeIndex = (currentJudgeIndex + 1) % room.roomInfo.playerList.size
                val newJudge = room.roomInfo.playerList[newJudgeIndex]
                room.roomInfo.judgePlayerID = newJudge.id
                room.roomInfo.playerList.map {
                    val newCards = room.words.take(it.presentedHand.size).toList()
                    it.session!!.sendMessage(Message(endOfRound = Message.EndOfRound(winnerPlayerID = winner?.id,
                        newCards = newCards,
                        // only let them choose a role if they are the new judge
                        roleCards = if (it == newJudge) {room.judgeRoleSequence.take(2).toList()} else {null}
                    )))
                }
            }

            room.mutex.withLock {
                val newPlayerID = if (room.roomInfo.playerList.isEmpty()) 0 else room.roomInfo.playerList.last().id + 1
                val newPlayer = Player(id = newPlayerID, session = this)
                room.roomInfo.playerList += newPlayer
                sendMessage(Message(currentState = Message.CurrentState(room.roomInfo, newPlayerID)))
                broadcastSkipSender(Message(playerJoined = Message.PlayerJoined(newPlayer)))
            }
            val sendingPlayer = room.roomInfo.playerList.first { it.session == this }
            try {
                while (true) {
                    val frame = incoming.receive()
                    if (frame is Frame.Text) {
                        room.mutex.withLock {
                            val message = json.decodeFromString(Message.serializer(), frame.readText())

                            if (message.setName != null) {
                                sendingPlayer.name = message.setName.name
                                broadcast(message)
                            } else if (message.selectionOfRole != null) {
                                sendingPlayer.judgeRole = message.selectionOfRole.role
                                broadcastSkipSender(message)
                            } else if (message.cardsPlayed != null) {
                                sendingPlayer.presentedHand = message.cardsPlayed.cards
                                broadcastSkipSender(message)
                            } else if (message.endOfRoundRequest != null) {
                                callEndOfRound(message.endOfRoundRequest.winnerPlayerID)
                            }
                        }
                        send(Frame.Text("Client said: " + frame.readText()))
                    }
                }
            } catch (e: Throwable) {
                println(e)
                globalMutex.withLock {
                    broadcastSkipSender(Message(playerLeft = Message.PlayerLeft(sendingPlayer.id)))
                    val isLeavingPlayerTheJudge = sendingPlayer.id == room.roomInfo.judgePlayerID
                    room.roomInfo.playerList.remove(sendingPlayer)
                    if (isLeavingPlayerTheJudge) {
                        callEndOfRound(null) // no winner
                    }
                    if (room.roomInfo.playerList.isEmpty())
                        rooms.remove(roomCode)
                }
            }

            get("/json/gson") {
                call.respond(mapOf("hello" to "world"))
            }
        }
    }
}

