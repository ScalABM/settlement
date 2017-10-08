package org.economicsl.settlement

import org.economicsl.core.{Quantity, Tradable}
import org.economicsl.settlement.contracts.SpotContract
import play.api.libs.json.{Json, Writes}


case class RequestAsset(asset: Tradable, quantity: Quantity)


object RequestAsset {

  implicit val writes: Writes[RequestAsset] = Json.writes[RequestAsset]

  def from(contract: SpotContract): RequestAsset = {
    RequestAsset(contract.tradable, contract.quantity)
  }

}

