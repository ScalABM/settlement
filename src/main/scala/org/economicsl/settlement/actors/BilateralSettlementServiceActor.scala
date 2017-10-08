package org.economicsl.settlement.actors

import org.economicsl.settlement.contracts.SpotContract


/** Bilateral clearing mechanism. */
class BilateralSettlementServiceActor
  extends SettlementServiceActor {

  def handleContract: Receive = {
    case contract: SpotContract =>
      val actorRefs = counterparties.get(contract.issuer).flatMap {
        actorRef => counterparties.get(contract.counterparty).map {
          otherActorRef => (actorRef, otherActorRef)
        }
      }

      context.actorOf(SpotContractHandler.props(contract))
  }

}
