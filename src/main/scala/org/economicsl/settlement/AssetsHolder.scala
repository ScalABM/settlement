package org.economicsl.settlement

import akka.actor.{Actor, ActorLogging}

import scala.util.{Failure, Success, Try}


trait AssetsHolder {
  this: Actor with ActorLogging =>

  /* For now assume that AssetsHolderLike can take negative asset positions. */
  val assets: mutable.Map[Asset, Double] = mutable.Map[Asset, Double]().withDefaultValue(0.0)

  /* Increments an actor's cash holdings. */
  def hoard(amount: Double): Unit = {
    assets(Currency) += amount
  }

  /* Decrements an actor's cash holdings. */
  def dishoard(amount: Double): Try[Payment] = {
    if (assets(Currency) >= amount) {
      assets(Currency) -= amount
      Success(Payment(amount))
    } else {
      Failure(InsufficientFundsException())
    }
  }

  /* Increment actor's securities holdings. */
  def accumulate(asset: Asset, quantity: Double): Unit = {
    assets(asset) += quantity
  }

  /* Decrement actor's securities holdings. */
  def deccumulate(asset: Asset, quantity: Double): Try[Assets] = {
    if (assets(asset) >= quantity) {
      assets(asset) -= quantity
      Success(Assets(asset, quantity))
    } else {
      Failure(InsufficientAssetsException())
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