package org.economicsl

import akka.actor.ActorRef


package object settlement {

  // need some kind of type that combines a Fill with the ActorRefs associated with the buyer and seller.
  type Buyer = ActorRef
  type Seller = ActorRef
  type SpotContract = (Buyer, Seller, Price, Quantity, Tradable)


  case class AssetsRequest(asset: Asset, quantity: Quantity)


  case class Assets(asset: Asset, quantity: Quantity)


  case class Payment(amount: Quantity)


  case class PaymentRequest(amount: Quantity)


  case class InsufficientFundsException(message: String = "Buyer has insufficient funds.") extends Exception(message)


  case class InsufficientAssetsException(message: String = "Seller has insufficient assets.") extends Exception(message)


}
