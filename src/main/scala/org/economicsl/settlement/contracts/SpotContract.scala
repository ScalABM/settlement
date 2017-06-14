package org.economicsl.settlement.contracts

import org.economicsl.settlement.{Price, Quantity, Tradable}
import play.api.libs.json.{Json, Writes}


case class SpotContract(issuer: BuyerRef, counterparty: SellerRef, price: Price, quantity: Quantity, tradable: Tradable)


object SpotContract {

  implicit val writes: Writes[SpotContract] = Json.writes[SpotContract]

}
