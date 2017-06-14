package org.economicsl.settlement

import play.api.libs.json.{Json, Writes}


/** Represents a cleared transaction between a buyer and a seller. */
case class Transaction(contract: SpotContract)


object Transaction {

  implicit val writes: Writes[Transaction] = Json.writes[Transaction]

}
