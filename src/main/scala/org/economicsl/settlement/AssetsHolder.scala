package org.economicsl.settlement

import akka.actor.{Actor, ActorLogging}

import scala.util.{Failure, Success, Try}


trait AssetsHolder {
  this: Actor with ActorLogging =>

  type Holdings = Map[Asset, Quantity]

  /* For now assume that AssetsHolderLike can take negative asset positions. */
  var holdings: Holdings

  /* Increments an actor's cash holdings. */
  def hoard(amount: Quantity): Holdings = {
    val current = holdings.getOrElse(Currency, Quantity(0))
    holdings.updated(Currency, current + amount)
  }

  /* Decrements an actor's cash holdings. */
  def dishoard(amount: Quantity): (Try[Payment], Holdings) = {
    val current = holdings.getOrElse(Currency, Quantity(0))
    if (current >= amount) {
      (Success(Payment(amount)), holdings.updated(Currency, current + amount))
    } else {
      (Failure(InsufficientFundsException()), holdings)
    }
  }

  /* Increment actor's securities holdings. */
  def accumulate(asset: Asset, quantity: Quantity): Holdings = {
    holdings(asset) += quantity
  }

  /* Decrement actor's securities holdings. */
  def deccumulate(asset: Asset, quantity: Quantity): (Try[Assets], Holdings) = {
    val current = holdings.getOrElse(asset, Quantity(0))
    if (current >= quantity) {
      (Success(Assets(asset, quantity)), holdings.updated(asset, current - quantity))
    } else {
      (Failure(InsufficientAssetsException()), holdings)
    }
  }

  def assetsHolderBehavior: Receive = {
    case Payment(amount) =>
      holdings = hoard(amount)
    case PaymentRequest(amount) =>
      val (payment, updated) = dishoard(amount)
      sender() ! payment
      holdings = updated
    case AssetsRequest(asset, quantity) =>
      val (assets, updated) = deccumulate(asset, quantity)
      sender() ! assets
      holdings = updated
    case Assets(asset, quantity) =>
      holdings = accumulate(asset, quantity)
  }

}