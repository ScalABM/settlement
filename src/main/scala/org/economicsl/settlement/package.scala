package org.economicsl


package object settlement {

  case class AssetsRequest(asset: AssetLike, quantity: Double)


  case class Assets(instrument: AssetLike, quantity: Double)


  case class Payment(amount: Double)


  case class PaymentRequest(amount: Double)


  case class InsufficientFundsException(message: String = "Buyer has insufficient funds.") extends Exception(message)


  case class InsufficientAssetsException(message: String = "Seller has insufficient assets.") extends Exception(message)


}
