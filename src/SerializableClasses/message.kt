package io.github.JMoore34_CSweetman.SerializableClasses

class Player(
    val name: String,
    val id: Int,
    val rolesWon: MutableList<String>,
)

class Room(
    val playerList: MutableList<Player>,
){

}

class Message(
    val playerJoinedMessage: PlayerJoined?
) {
    class PlayerJoined(val newPlayer: Player)
}