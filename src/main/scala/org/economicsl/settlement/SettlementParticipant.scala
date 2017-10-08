package org.economicsl.settlement

import java.util.UUID

/** Base trait for all `SettlementService` participants.
  *
  * @author davidrpugh
  * @since 0.1.0
  */
trait SettlementParticipant {

  /** Each `SettlementParticipant` must have a unique identifier. */
  def uuid: UUID

}
