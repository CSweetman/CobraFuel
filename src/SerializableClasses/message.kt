package io.github.JMoore34_CSweetman.SerializableClasses

class Player(
    val name: String,
    val id: Int,
    val rolesWon: MutableList<String>,
    val presentedHand: MutableList<String>
)

class SerializableRoomInfo(
    val playerList: MutableList<Player>,
    val judgePlayerID: Int,
    val judgeRole: String
){

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
    class CurrentState(roomData: SerializableRoomInfo, playerID: Int)

    // Informs players of a new player who joined.
    // Server -> Client only.
    class PlayerJoined(val newPlayer: Player)

    // Informs players that a player left.
    // Server -> Client only.
    class PlayerLeft(val playerID: Int)

    // Informs players of the winner (if there is one), the new cards they have drawn, and a list of roles to choose
    // from iff the player is now the judge (this list is null otherwise).
    // Server -> Client only.
    class EndOfRound(winnerPlayerID: Int?, newCards: List<String>, roleCards: List<String>?)

    class SelectionOfRole(Role: String, judgeID: Int)

    class CardsPlayed(cardsPlayed: List<String>)
}}

