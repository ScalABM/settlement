package org.economicsl.settlement.actors

import java.util.UUID

import akka.actor.{Actor, ActorLogging, ActorRef, Terminated}


trait SettlementServiceActor
    extends StackableActor {

  import SettlementServiceActor._

  def handleContract: Receive

  def registerCounterparty: Receive = {
    case DeregisterCounterparty(uuid) =>
      counterparties.get(uuid).foreach(context.unwatch)
      counterparties = counterparties - uuid
    case RegisterCounterparty(uuid, actorRef) =>
      context.watch(actorRef)
      counterparties + (uuid -> actorRef)
    case Terminated(counterparty) =>
      context.unwatch(counterparty)
      val terminated = counterparties.find{ case (uuid, actorRef) => actorRef == counterparty }
      ???  // todo: idiomatic removal of terminated from counterparties...
  }

  def receive: Receive = {
    handleContract orElse registerCounterparty
  }

  protected var counterparties: Map[UUID, ActorRef] = Map.empty[UUID, ActorRef]

}


object SettlementServiceActor {

  final case class DeregisterCounterparty(uuid: UUID)

  final case class RegisterCounterparty(uuid: UUID, actorRef: ActorRef)

}
