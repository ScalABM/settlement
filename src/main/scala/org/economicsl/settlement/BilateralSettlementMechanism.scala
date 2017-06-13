package org.economicsl.settlement


/** Bilateral clearing mechanism. */
class BilateralSettlementMechanism extends SettlementMechanism {

  val settlementMechanismBehavior: Receive = {
    case fill: FillLike =>
      context.actorOf(TransactionHandler.props(fill))
  }

  def receive: Receive = {
    settlementMechanismBehavior
  }

}
