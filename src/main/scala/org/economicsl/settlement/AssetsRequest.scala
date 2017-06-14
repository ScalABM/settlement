package org.economicsl.settlement

import org.economicsl.settlement.contracts.SpotContract
import play.api.libs.json.{Json, Writes}


case class AssetsRequest(asset: Asset, quantity: Quantity)

object AssetsRequest {

  implicit val writes: Writes[AssetsRequest] = Json.writes[AssetsRequest]

  def from(contract: SpotContract): AssetsRequest = {
    AssetsRequest(contract.tradable, contract.quantity)
  }

}

