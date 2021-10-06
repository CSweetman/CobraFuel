package io.github.JMoore34_CSweetman.SerializableClasses
import kotlinx.serialization.*

@Serializable
class Player(
    val name: String,
    val id: Int,
    val rolesWon: MutableList<String>,
    val presentedHand: MutableList<String>
)

@Serializable
class SerializableRoomInfo(
    val playerList: MutableList<Player>,
    val judgePlayerID: Int,
    val judgeRole: String
)

@Serializable
class Message(
    // Exactly one of the following will be non-null.
    val currentState: CurrentState?,
    val playerJoined: PlayerJoined?,
    val playerLeft: PlayerLeft?,
    val endOfRound: EndOfRound?,
    val selectionOfRole: SelectionOfRole?,
    val cardsPlayed: CardsPlayed?

) {
    // Informs a new player of the current room state & their assigned player ID.
    // Server -> Client only.
    @Serializable
    class CurrentState(val roomData: SerializableRoomInfo, val playerID: Int)

    // Informs players of a new player who joined.
    // Server -> Client only.
    @Serializable
    class PlayerJoined(val newPlayer: Player)

    // Informs players that a player left.
    // Server -> Client only.
    @Serializable
    class PlayerLeft(val playerID: Int)

    // Informs players of the winner (if there is one), the new cards they have drawn, and a list of roles to choose
    // from iff the player is now the judge (this list is null otherwise).
    // Server -> Client only.
    @Serializable
    class EndOfRound(val winnerPlayerID: Int?, val newCards: List<String>, val roleCards: List<String>?)

    // 1. Sent from judge player to server when they choose between their role options.
    // 2. Broadcast from server to all clients (server sets judge ID).
    // Judge player -> Server -> All clients
    @Serializable
    class SelectionOfRole(val Role: String, val judgeID: Int?)

    // 1. Sent from player to server when they select the cards they want to present.
    // 2. Broadcast from server to all clients (server sets player ID).
    @Serializable
    class CardsPlayed(val cardsPlayed: List<String>, val playerID: Int?)
}

