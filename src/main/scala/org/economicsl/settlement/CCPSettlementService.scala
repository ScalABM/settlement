package org.economicsl.settlement

import java.util.UUID

import org.economicsl.core.Quantity
import org.economicsl.settlement.actors.BilateralSettlementServiceActor
import org.economicsl.settlement.contracts.{Contract, SpotContract}

import scala.util.Try


/** Central counterparty (CCP) settlement service.
  *
  * @note The key difference between CCP clearing and bilateral clearing is that
  * CCP inserts itself as the counterparty to both the ask and the bid
  * trading parties before processing the final transaction. By acting as
  * a counterparty on every transaction the CCP effectively assumes all
  * counterparty risk.
  */
class CCPSettlementService private(
  val blockChain: Stream[Block],
  val holdings: AssetHoldings,
  val participants: Map[UUID, SettlementParticipant])
    extends SettlementService[CCPSettlementService]
    with Counterparty {

  /* BilateralClearingMechanism can be used to process novated fills. */
  val bilateralClearingMechanism: ActorRef = context.actorOf(Props[BilateralSettlementServiceActor])

  /** Central counter-party (CCP) clearing mechanism behavior. */
  val settlementMechanismBehavior: Receive = {
    case contract: SpotContract =>
      val novatedFills = novate(contract)
      novatedFills foreach(novatedFill => bilateralClearingMechanism ! novatedFill)
  }


  def settle(contract: Contract): Try[(CCPSettlementService, Block)] = {
    val novatedContracts = novate(contract)

    val counterparty: UUID = ???
    val issuer: UUID = ???
    ???

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
  protected def novate(contract: Contract): (Contract, Contract) = {
    (contract.copy(issuer = self), contract.copy(counterparty = self))
  }

  /** Factory method used to delegate instance creation to sub-classes. */
  protected def withBlock(additional: Block): CCPSettlementService = {
    new CCPSettlementService(additional #:: blockChain, holdings, participants)
  }

  protected def withHoldings(holdings: AssetHoldings): CCPSettlementService = {
    new CCPSettlementService(blockChain, holdings, participants)
  }

  protected def withParticipant(additional: SettlementParticipant): CCPSettlementService = {
    new CCPSettlementService(blockChain, holdings, participants + (additional.uuid -> additional))
  }

}


object CCPSettlementService {

  def empty: CCPSettlementService = {
    val emptyBlockChain = Stream.empty[Block]
    val deepPockets = Map.empty[UUID, Quantity].withDefaultValue(Quantity.MaxValue)
    new CCPSettlementService(emptyBlockChain, deepPockets)
  }

}
