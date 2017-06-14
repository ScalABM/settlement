package org.economicsl.settlement


sealed trait SettlementException extends Exception

case class InsufficientFundsException(message: String = "Buyer has insufficient funds.") extends SettlementException

case class InsufficientAssetsException(message: String = "Seller has insufficient assets.") extends SettlementException

