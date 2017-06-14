package org.economicsl.settlement

import akka.actor.ActorRef


package object contracts {

  // need some kind of type that combines a Fill with the ActorRefs associated with the buyer and seller.
  type BuyerRef = ActorRef
  type SellerRef = ActorRef

}
