package org.economicsl.settlement


/** Central counterparty (CCP) clearing mechanism.
  *
  * @note The key difference between CCP clearing and bilateral clearing is that
  * CCP inserts itself as the counterparty to both the ask and the bid
  * trading parties before processing the final transaction. By acting as
  * a counterparty on every transaction the CCP effectively assumes all
  * counterparty risk.
  */
class CCPSettlementMechanism extends SettlementMechanism with AssetsHolder {

  /* For now assume that central counterparty has "deep pockets". */
  override val assets: mutable.Map[Asset, Double] = {
    mutable.Map[Asset, Double]().withDefaultValue(Double.PositiveInfinity)
  }

  /* BilateralClearingMechanism can be used to process novated fills. */
  val bilateralClearingMechanism: ActorRef = context.actorOf(Props[BilateralSettlementMechanism])

  /** Central counter-party (CCP) clearing mechanism behavior. */
  val settlementMechanismBehavior: Receive = {
    case fill: FillLike =>
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
  def novate(fill: FillLike): List[FillLike] = fill match {
    case fill: PartialFill =>
      List(PartialFill(self, fill.bidTradingPartyRef, fill.instrument, fill.price, fill.quantity),
        PartialFill(fill.askTradingPartyRef, self, fill.instrument, fill.price, fill.quantity))
    case fill: TotalFill =>
      List(TotalFill(self, fill.bidTradingPartyRef, fill.instrument, fill.price, fill.quantity),
        TotalFill(fill.askTradingPartyRef, self, fill.instrument, fill.price, fill.quantity))
  }

  def receive: Receive = {
    settlementMechanismBehavior orElse assetsHolderBehavior
  }

}
