package org.economicsl.settlement

import org.economicsl.settlement.contracts.SpotContract
import play.api.libs.json.{Json, Writes}


case class PaymentRequest(amount: Long)


object PaymentRequest {

  implicit val writes: Writes[PaymentRequest] = Json.writes[PaymentRequest]

  def from(contract: SpotContract): PaymentRequest = {
    PaymentRequest(contract.price * contract.quantity)
  }

}
