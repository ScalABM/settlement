package org.economicsl.settlement

import org.economicsl.auctions.Tradable
import org.economicsl.auctions.singleunit.Fill
import play.api.libs.json.{Json, Writes}


/** Represents a cleared transaction between a buyer and a seller. */
case class Transaction[T <: Tradable](fill: Fill[T])


object Transaction {

  implicit def writes[T <: Tradable]: Writes[Transaction[T]] = Json.writes[Transaction[T]]

}
