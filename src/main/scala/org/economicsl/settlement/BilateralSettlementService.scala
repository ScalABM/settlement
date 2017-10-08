package org.economicsl.settlement

import java.util.UUID

import org.economicsl.settlement.contracts.SpotContract

import scala.util.Try


class BilateralSettlementService private(
  val blockChain: Stream[Block],
  val participants: Map[UUID, SettlementParticipant])
    extends SettlementService[BilateralSettlementService] {

  def settle(contract: SpotContract): Try[(BilateralSettlementService, Block)] = {
    val buyerResponse: Try[(SettlementParticipant, Payment)] = participants.get(contract.issuer).flatMap(buyer => buyer.dishoard(???))
    val sellerResponse: Try[(SettlementParticipant, Assets)] = ???

  }

  /** Factory method used to delegate instance creation to sub-classes. */
  protected def withBlock(additional: Stream[Transaction]): BilateralSettlementService = {
    new BilateralSettlementService(additional #:: blockChain, participants)
  }

  /** Factory method used to delegate instance creation to sub-classes. */
  protected def withParticipant(additional: SettlementParticipant): BilateralSettlementService = {
    new BilateralSettlementService(blockChain, participants + (additional.uuid -> additional))
  }

}
