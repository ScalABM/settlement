package org.economicsl.settlement.actors


import org.economicsl.settlement._

import scala.util.{Failure, Success}


trait CounterpartyActor
  extends StackableActor {

  def assetsHolderBehavior: Receive = {
    case Payment(currency, amount) =>
      counterparty = counterparty.hoard(currency, amount)
    case Assets(tradable, quantity) =>
      counterparty = counterparty.accumulate(tradable, quantity)
    case RequestPayment(currency, amount) =>
      counterparty.dishoard(currency, amount) match {
        case Success((updated, payment)) =>
          counterparty = updated
          sender() ! Success(payment)
        case failure @ Failure(_) =>
          sender() ! failure
      }
    case RequestAsset(asset, quantity) =>
      counterparty.deccumulate(asset, quantity) match {
        case Success((updated, assets)) =>
          counterparty = updated
          sender ! Success(assets)
        case failure @ Failure(_) =>
          sender() ! failure
      }
  }

  protected var counterparty: Counterparty

}