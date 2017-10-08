package org.economicsl.settlement

import org.economicsl.core.securities.Security
import org.economicsl.core.Quantity
import org.economicsl.settlement.contracts.SpotContract
import play.api.libs.json.{Json, Writes}


case class RequestPayment(currency: Security, quantity: Quantity)


object RequestPayment {

  implicit val writes: Writes[RequestPayment] = Json.writes[RequestPayment]

  def from(contract: SpotContract): RequestPayment = {
    RequestPayment(???, contract.price * contract.quantity)
  }

}
