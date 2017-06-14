package org.economicsl.settlement

import akka.actor.{PoisonPill, Props}
import org.economicsl.auctions.Tradable
import org.economicsl.auctions.singleunit.Fill

import scala.util.{Failure, Success, Try}


/** Handles clearing of an individual transaction between a buyer and a seller.
  *
  * @param fill
  */
class TransactionHandler[T <: Tradable](fill: Fill[T]) extends ContractHandler {

  /* Primary constructor */
  val seller = fill.askOrder.issuer
  val buyer = fill.bidOrder.issuer

  seller ! AssetsRequest(fill.instrument, fill.quantity)
  buyer ! PaymentRequest(fill.price * fill.quantity)

  /* Only evaluated if necessary! */
  lazy val transaction = Transaction(fill)

  /** Behavior of a TransactionHandler after receiving the seller's response.
    *
    * @param sellerResponse
    * @return a partial function that handles the buyer's response.
    */
  def awaitingBuyerResponse(sellerResponse: Try[Assets]): Receive = sellerResponse match {
    case Success(assets) => {  // partial function for handling buyer response given successful seller response
      case Success(payment) =>
        buyer ! assets
        seller ! payment
        self ! PoisonPill
      case Failure(ex) =>
        seller ! assets  // refund assets to seller
        self ! PoisonPill
    }
    case Failure(exception) => {  // partial function for handling buyer response given failed seller response
      case Success(payment) =>  // refund payment to buyer
        buyer ! payment
        self ! PoisonPill
      case Failure(otherException) => // nothing to refund
        self ! PoisonPill
    }
  }

  /** Behavior of a TransactionHandler after receiving the buyer's response.
    *
    * @param buyerResponse
    * @return partial function that handles the seller's response.
    */
  def awaitingSellerResponse(buyerResponse: Try[Payment]): Receive = buyerResponse match {
    case Success(payment) => {  // partial function for handling seller response given successful buyer response
      case Success(assets) =>
        buyer ! assets
        seller ! payment
        self ! PoisonPill
      case Failure(exception) =>
        buyer ! payment  // refund payment to buyer
        self ! PoisonPill
    }
    case Failure(exception) => {  // partial function for handling seller response given failed buyer response
      case Success(assets) =>  // refund assets to seller
        seller ! assets
        self ! PoisonPill
      case Failure(otherException) =>  // nothing to refund
        self ! PoisonPill
    }
  }

  /** Behavior of a TransactionHandler.
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


object TransactionHandler {

  def props[T <: Tradable](fill: Fill[T]): Props = {
    Props(new TransactionHandler(fill[T]))
  }

}