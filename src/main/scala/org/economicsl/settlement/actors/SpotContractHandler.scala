package org.economicsl.settlement.actors

import akka.actor.{ActorIdentity, PoisonPill, Props}
import org.economicsl.settlement._
import org.economicsl.settlement.contracts.SpotContract
import play.api.libs.json.Json

import scala.util.{Failure, Success, Try}


/** Handles clearing of an individual transaction between a buyer and a seller.
  *
  * @author davidrpugh
  */
class SpotContractHandler(contract: SpotContract)
    extends ContractHandler {

  context.parent ! RequestActorIdentity("issuer", contract.issuer)
  context.parent ! RequestActorIdentity("counterparty", contract.issuer)

  def identifying: Receive = {
    case ActorIdentity("issuer", maybeActorRef) =>
      maybeActorRef.foreach(actorRef => context.become(identifyingCounterparty(actorRef)))
    case ActorIdentity("counterparty", maybeActorRef) =>
      maybeActorRef.foreach(actorRef => context.become(identifyingIssuer(actorRef)))
  }

  /** Behavior of a SpotContractHandler after receiving the seller's response.
    *
    * @param sellerResponse
    * @return a partial function that handles the buyer's response.
    */
  def awaitingBuyerResponse(sellerResponse: Try[Assets]): Receive = sellerResponse match {
    case Success(assets) => {
      case Success(payment) =>
        buyer ! assets
        seller ! payment
        self ! PoisonPill
      case Failure(_) =>
        seller ! assets  // refund assets to seller
        self ! PoisonPill
    }
    case Failure(_) => {
      case Success(payment) =>  // refund payment to buyer
        buyer ! payment
        self ! PoisonPill
      case Failure(_) => // nothing to refund
        self ! PoisonPill
    }
  }

  /** Behavior of a SpotContractHandler after receiving the buyer's response.
    *
    * @param buyerResponse
    * @return partial function that handles the seller's response.
    */
  def awaitingSellerResponse(buyerResponse: Try[Payment]): Receive = buyerResponse match {
    case Success(payment) => {
      case Success(assets) =>
        buyer ! assets
        seller ! payment
        self ! PoisonPill
        log.info(Json.toJson(transaction).toString)
      case Failure(_) =>
        buyer ! payment  // refund payment to buyer
        self ! PoisonPill
    }
    case Failure(_) => {
      case Success(assets) =>  // refund assets to seller
        seller ! assets
        self ! PoisonPill
      case Failure(_) =>  // nothing to refund
        self ! PoisonPill
    }
  }

  /** Behavior of a SpotContractHandler.
    *
    * @return partial function that handles buyer and seller responses.
    */
  def receive: Receive = {

    case Success(Payment(currency, amount)) =>
      context.become(awaitingSellerResponse(Success(Payment(currency, amount))))
    case Failure(InsufficientFundsException(msg)) =>
      context.become(awaitingSellerResponse(Failure(InsufficientFundsException(msg))))
    case Success(Assets(tradable, quantity)) =>
      context.become(awaitingBuyerResponse(Success(Assets(tradable, quantity))))
    case Failure(InsufficientAssetsException(msg)) =>
      context.become(awaitingBuyerResponse(Failure(InsufficientAssetsException(msg))))

  }

}


object SpotContractHandler {

  def props(contract: SpotContract): Props = {
    Props(new SpotContractHandler(contract))
  }

}