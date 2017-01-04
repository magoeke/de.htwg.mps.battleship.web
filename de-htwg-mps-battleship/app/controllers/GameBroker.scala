package controllers

import java.util.UUID
import javax.inject.{Inject, Singleton}

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import com.google.inject.ImplementedBy
import de.htwg.mps.battleship.controller.command.Command

import scala.collection.mutable.Map

@ImplementedBy(classOf[GameBroker])
trait IGameBroker {
  def actorRef: ActorRef
}

@Singleton
class GameBroker @Inject() (implicit system: ActorSystem) extends IGameBroker {
  override def actorRef: ActorRef = system.actorOf(Props[GameBrokerActor])

  class GameBrokerActor extends Actor {

    val maxParticipants: Int = 2
    var participants: Int = 0
    var currentGame: UUID = UUID.randomUUID()
    var games: Map[UUID, ActorRef] = Map[UUID, ActorRef]()

    override def receive = {
      case JoinGame => sender() ! Game(join(sender())); games(currentGame) ! JoinGame(sender())
      case CommandProxy(id, command) => games(id) ! command
    }

    private def join(actorRef: ActorRef) = {
      if(participants >= maxParticipants) { reset }
      participants += 1
      if(!games.exists(_._1 == currentGame)) { gameActor }
      currentGame
    }

    private def gameActor = system.actorOf(Props(new GameActor(system)))

    private def reset = {
      participants = 0
      currentGame = UUID.randomUUID()
    }
  }
}

// Messages
case object JoinGame
case class JoinGame(actorRef: ActorRef)
case class Game(id: UUID)
case class CommandProxy(id: UUID, command: Command)