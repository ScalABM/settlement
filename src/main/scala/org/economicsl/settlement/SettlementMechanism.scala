package org.economicsl.settlement

import akka.actor.{Actor, ActorLogging}


trait SettlementMechanism extends Actor with ActorLogging {

  def settlementMechanismBehavior: Receive

}
