package org.economicsl.settlement.contracts

import java.util.UUID

import org.economicsl.core.{Price, Quantity, Tradable}
import play.api.libs.json.{Json, Writes}

/**
  *
  * @param issuer
  * @param counterparty
  * @param price
  * @param quantity
  * @param tradable
  * @author davidrpugh
  * @since 0.1.0
  */
case class SpotContract(issuer: UUID, counterparty: UUID, price: Price, quantity: Quantity, tradable: Tradable)
  extends Contract


object SpotContract {

  implicit val writes: Writes[SpotContract] = Json.writes[SpotContract]

}