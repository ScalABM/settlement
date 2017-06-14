package org.economicsl.settlement.contracts

import akka.actor.{PoisonPill, Props}
import org.economicsl.settlement._
import play.api.libs.json.Json

import scala.util.{Failure, Success, Try}


/** Handles clearing of an individual transaction between a buyer and a seller.
  *
  * @author davidrpugh
  */
class SpotContractHandler[T <: Tradable](contract: SpotContract) extends ContractHandler {
  
  /* Executed as part of primary constructor */
  private[this] val (buyer, seller) = (contract.issuer, contract.counterparty)
  seller ! AssetsRequest.from(contract)
  buyer ! PaymentRequest.from (contract)  // units are currency!

  /* Only evaluated if necessary! */
  lazy val transaction = Transaction(contract)

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
      case Failure(ex) =>
        seller ! assets  // refund assets to seller
        self ! PoisonPill
    }
    case Failure(exception) => {
      case Success(payment) =>  // refund payment to buyer
        buyer ! payment
        self ! PoisonPill
      case Failure(otherException) => // nothing to refund
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
      case Failure(exception) =>
        buyer ! payment  // refund payment to buyer
        self ! PoisonPill
    }
    case Failure(exception) => {
      case Success(assets) =>  // refund assets to seller
        seller ! assets
        self ! PoisonPill
      case Failure(otherException) =>  // nothing to refund
        self ! PoisonPill
    }
  }

  /** Behavior of a SpotContractHandler.
    *
    * @return partial function that handles buyer and seller responses.
    */
  def receive: Receive = {

    case Success(Payment(amount)) =>
      context.become(awaitingSellerResponse(Success(Payment(amount))))
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