package org.economicsl

import org.economicsl.auctions.Quantity


package object settlement {

  case class AssetsRequest(asset: Asset, quantity: Quantity)


  case class Assets(instrument: Asset, quantity: Quantity)


  case class Payment(amount: Quantity)


  case class PaymentRequest(amount: Quantity)


  case class InsufficientFundsException(message: String = "Buyer has insufficient funds.") extends Exception(message)


  case class InsufficientAssetsException(message: String = "Seller has insufficient assets.") extends Exception(message)


}
