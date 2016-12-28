package models

import java.util.UUID

import scala.collection.mutable.ListBuffer
import scala.collection.mutable.Map

object WebState {
  private val gamesMap = Map[UUID, GameEntry]()
//  private val lobby = new Map[UUID, ListBuffer[UUID]]()

  def addGame(name: String, maxPlayers: Int) = {
    gamesMap += (UUID.randomUUID() -> GameEntry(name, List[UUID](), maxPlayers))
  }

  def joinLobby(lobbyID: UUID, playerID: UUID) = {
    gamesMap(lobbyID) = gamesMap(lobbyID).copy(players = playerID :: gamesMap(lobbyID).players)
  }

  def leaveLobby(lobbyID: UUID, playerID: UUID) = {
    gamesMap(lobbyID) = gamesMap(lobbyID).copy(players = gamesMap(lobbyID).players diff List(playerID))
  }

  def games : Map[UUID, GameEntry] = gamesMap
}

case class GameEntry(name: String, players: List[UUID], maxPlayers: Int) {
  def taken = players.length
}