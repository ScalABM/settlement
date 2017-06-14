package org.economicsl.settlement


/** Bilateral clearing mechanism. */
class BilateralSettlementMechanism extends SettlementMechanism {

  val settlementMechanismBehavior: Receive = {
    case contract: SpotContract => context.actorOf(SpotContractHandler.props(contract))
  }

  def receive: Receive = {
    settlementMechanismBehavior
  }

}
