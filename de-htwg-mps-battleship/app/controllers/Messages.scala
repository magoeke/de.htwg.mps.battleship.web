package controllers

import java.util.UUID

import scala.collection.mutable.Map

trait Message

case class RegisterLobbyActor(lobbyID: UUID)
case class DeregisterLobbyActor(lobbyID: UUID)
case class BroadcastMessage(lobby: UUID, sender: UUID, message: String)
case class SendMessage(msg: String) extends Message

case object RegisterActor
case object DeregisterActor
case class UpdateGameBrowser(games: Map[UUID, GameEntry])
case class JoinLobby(lobbyID: UUID, playerID:UUID)
case class LeaveLobby(lobbyID: UUID, playerID:UUID)
case class AddGame(name: String, maxPlayers: Int)