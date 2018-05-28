package org.economicsl.settlement

import org.economicsl.core.securities.Security
import org.economicsl.core.{Quantity, Tradable}

import scala.util.{Failure, Success, Try}


trait AssetHolder[A <: AssetHolder[A]] {

  /* Increments "cash" holdings. */
  def hoard(payment: Payment): A = {
    val currencyHoldings = holdings.getOrElse(payment.currency.uuid, Quantity.zero)
    val updatedCurrencyHoldings = holdings.updated(payment.currency.uuid, currencyHoldings + payment.quantity)
    withHoldings(updatedCurrencyHoldings)
  }

  /* Decrements "cash" holdings. */
  def dishoard(currency: Security, amount: Quantity): Try[(A, Payment)] = {
    val current = holdings.getOrElse(currency.uuid, Quantity.zero)
    if (current >= amount) {
      val updatedHoldings = holdings.updated(currency.uuid, current - amount)
      Success((withHoldings(updatedHoldings), Payment(currency, amount)))
    } else {
      Failure(InsufficientFundsException())
    }
  }

  /* Increments tradable asset holdings. */
  def accumulate(assets: Assets): A = {
    val current = holdings.getOrElse(asset.uuid, Quantity.zero)
    val updatedHoldings = holdings.updated(asset.uuid, current + quantity)
    withHoldings(updatedHoldings)
  }

  /* Decrement tradable asset holdings. */
  def deccumulate(asset: Tradable, quantity: Quantity): Try[(A, Assets)] = {
    val current = holdings.getOrElse(asset.uuid, Quantity.zero)
    if (current >= quantity) {
      val updatedHoldings = holdings.updated(asset.uuid, current - quantity)
      Success((withHoldings(updatedHoldings), Assets(asset, quantity)))
    } else {
      Failure(InsufficientAssetsException())
    }
  }

  def holdings: AssetHoldings

  protected def withHoldings(holdings: AssetHoldings): A

}