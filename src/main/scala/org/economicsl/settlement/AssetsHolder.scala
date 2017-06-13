package org.economicsl.settlement

import akka.actor.{Actor, ActorLogging}
import org.economicsl.auctions.Quantity

import scala.util.{Failure, Success, Try}


trait AssetsHolder {
  this: Actor with ActorLogging =>

  type Assets = Map[Asset, Quantity]

  /* For now assume that AssetsHolderLike can take negative asset positions. */
  var assets: Assets = Map.empty[Asset, Quantity]

  /* Increments an actor's cash holdings. */
  def hoard(amount: Quantity): Assets = {
    val current = assets.getOrElse(Currency, Quantity(0))
    assets.updated(Currency, current + amount)
  }

  /* Decrements an actor's cash holdings. */
  def dishoard(amount: Quantity): (Try[Payment], Assets) = {
    val current = assets.getOrElse(Currency, Quantity(0))
    if (current >= amount) {
      (Success(Payment(amount)), assets.updated(Currency, current + amount))
    } else {
      (Failure(InsufficientFundsException()), assets)
    }
  }

  /* Increment actor's securities holdings. */
  def accumulate(asset: Asset, quantity: Quantity): Assets = {
    assets(asset) += quantity
  }

  /* Decrement actor's securities holdings. */
  def deccumulate(asset: Asset, quantity: Quantity): (Try[Assets], Assets) = {
    val current = assets.getOrElse(asset, Quantity(0))
    if (current >= quantity) {
      (Success(Assets(asset, quantity)), assets.updated(asset, current - quantity))
    } else {
      (Failure(InsufficientAssetsException()), assets)
    }
  }

  def assetsHolderBehavior: Receive = {
    case Payment(amount) =>
      hoard(amount)
    case PaymentRequest(amount) =>
      val payment = dishoard(amount)
      sender() ! payment
    case AssetsRequest(asset, quantity) =>
      val assets = deccumulate(asset, quantity)
      sender() ! assets
    case Assets(asset, quantity) =>
      accumulate(asset, quantity)
  }

}