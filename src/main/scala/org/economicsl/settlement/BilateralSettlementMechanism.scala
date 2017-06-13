package org.economicsl.settlement

import org.economicsl.auctions.Tradable
import org.economicsl.auctions.singleunit.Fill


/** Bilateral clearing mechanism. */
class BilateralSettlementMechanism[T <: Tradable] extends SettlementMechanism {

  val settlementMechanismBehavior: Receive = {
    case fill: Fill[T] =>
      context.actorOf(TransactionHandler.props(fill))
  }

  def receive: Receive = {
    settlementMechanismBehavior
  }

}
