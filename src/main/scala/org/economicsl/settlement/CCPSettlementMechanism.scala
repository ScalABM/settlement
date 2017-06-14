package org.economicsl.settlement

import akka.actor.{ActorRef, Props}


/** Central counterparty (CCP) clearing mechanism.
  *
  * @note The key difference between CCP clearing and bilateral clearing is that
  * CCP inserts itself as the counterparty to both the ask and the bid
  * trading parties before processing the final transaction. By acting as
  * a counterparty on every transaction the CCP effectively assumes all
  * counterparty risk.
  */
class CCPSettlementMechanism extends SettlementMechanism with AssetsHolder {

  /* For now assume that CCP has "deep pockets" */
  var holdings: Holdings = Map.empty[Asset, Quantity].withDefaultValue(Quantity.MaxValue)

  /* BilateralClearingMechanism can be used to process novated fills. */
  val bilateralClearingMechanism: ActorRef = context.actorOf(Props[BilateralSettlementMechanism])

  /** Central counter-party (CCP) clearing mechanism behavior. */
  val settlementMechanismBehavior: Receive = {
    case contract: SpotContract =>
      val novatedFills = novate(contract)
      novatedFills foreach(novatedFill => bilateralClearingMechanism ! novatedFill)
  }

  /** Novate a FillLike between two trading counterparties.
    *
    * @note The substitution of counterparties is typically accomplished through
    *       a legal process called contract novation. Novation discharges the
    *       contract between the original trading counterparties and creates two new,
    *       legally binding contracts – one between each of the original trading
    *       counterparties and the central counterparty.
    * @param contract a Fill between two trading counterparties.
    * @return a list of two Fill - one between each of the original trading
    *         counterparties and the central counterparty.
    */
  def novate(contract: SpotContract): List[SpotContract] = {
    List((self, contract._2, contract._3, contract._4, contract._5), (contract._1, self, contract._3, contract._4, contract._5))
  }

  def receive: Receive = {
    settlementMechanismBehavior orElse assetsHolderBehavior
  }

}
