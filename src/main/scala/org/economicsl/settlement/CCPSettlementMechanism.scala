package org.economicsl.settlement

import akka.actor.{ActorRef, Props}
import org.economicsl.auctions.Tradable
import org.economicsl.auctions.singleunit.Fill


/** Central counterparty (CCP) clearing mechanism.
  *
  * @note The key difference between CCP clearing and bilateral clearing is that
  * CCP inserts itself as the counterparty to both the ask and the bid
  * trading parties before processing the final transaction. By acting as
  * a counterparty on every transaction the CCP effectively assumes all
  * counterparty risk.
  */
class CCPSettlementMechanism[T <: Tradable] extends SettlementMechanism with AssetsHolder {

  /* For now assume that central counterparty has "deep pockets". */
  override val holdings: mutable.Map[Asset, Double] = {
    mutable.Map[Asset, Double]().withDefaultValue(Double.PositiveInfinity)
  }

  /* BilateralClearingMechanism can be used to process novated fills. */
  val bilateralClearingMechanism: ActorRef = context.actorOf(Props[BilateralSettlementMechanism[T]])

  /** Central counter-party (CCP) clearing mechanism behavior. */
  val settlementMechanismBehavior: Receive = {
    case fill: Fill[T] =>
      val novatedFills = novate(fill)
      novatedFills foreach(novatedFill => bilateralClearingMechanism ! novatedFill)
  }

  /** Novate a FillLike between two trading counterparties.
    *
    * @note The substitution of counterparties is typically accomplished through
    *       a legal process called contract novation. Novation discharges the
    *       contract between the original trading counterparties and creates two new,
    *       legally binding contracts – one between each of the original trading
    *       counterparties and the central counterparty.
    * @param fill a FillLike between two trading counterparties.
    * @return a list of two FillLikes - one between each of the original trading
    *         counterparties and the central counterparty.
    */
  def novate(fill: Fill[T]): List[Fill[T]] = {
    List(Fill(self, fill.bidTradingPartyRef, fill.instrument, fill.price, fill.quantity),
         Fill(fill.askTradingPartyRef, self, fill.instrument, fill.price, fill.quantity))
  }

  def receive: Receive = {
    settlementMechanismBehavior orElse assetsHolderBehavior
  }

}
