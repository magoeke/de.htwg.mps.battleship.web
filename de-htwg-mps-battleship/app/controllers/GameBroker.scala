package controllers

import javax.inject.{Inject, Singleton}

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import com.google.inject.ImplementedBy
import de.htwg.mps.battleship.controller.command.SetShip

@ImplementedBy(classOf[GameBroker])
trait IGameBroker {
  def actorRef: ActorRef
}

@Singleton
class GameBroker @Inject() (implicit system: ActorSystem) extends IGameBroker {
  val gameBrokerActor = system.actorOf(Props(new GameBrokerActor(system)))
  override def actorRef: ActorRef = gameBrokerActor
}

class GameBrokerActor(system: ActorSystem) extends Actor {

  val maxParticipants: Int = 2
  var participants: Int = 0
  var currentActorRef: ActorRef = gameActorRef

  override def receive = {
    case JoinGame(name, ships) => sender() ! Game(join(sender())); currentActorRef ! JoinSpecificGame(name, ships, sender())
  }

  private def join(actorRef: ActorRef) = {
    if(participants >= maxParticipants) { reset() }
    participants += 1
    currentActorRef
  }

  private def gameActorRef = system.actorOf(Props(new GameActor(system)))

  private def reset() = {
    participants = 0
    currentActorRef = gameActorRef
  }
}

// Messages
case class JoinGame(name: String, ships: List[SetShip])
case class JoinSpecificGame(name: String, ships: List[SetShip], actorRef: ActorRef)
case class Game(gameActorRef: ActorRef)