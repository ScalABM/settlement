package org.economicsl.settlement

import akka.actor.{ActorRef, Props, ActorSystem}
import akka.testkit.{TestProbe, TestActorRef, TestKit}
import models._
import org.scalatest.{BeforeAndAfterAll, Matchers, GivenWhenThen, FeatureSpecLike}

import scala.util.Random


class BilateralClearingMechanismSpec extends TestKit(ActorSystem("NoiseTraderSpec")) with
  FeatureSpecLike with
  GivenWhenThen with
  Matchers with
  BeforeAndAfterAll {

  /** Shutdown actor system when finished. */
  override def afterAll(): Unit = {
    system.shutdown()
  }

  def generateRandomPartialFill(askTradingPartyRef: ActorRef,
                                bidTradingPartyRef: ActorRef,
                                instrument: Stock,
                                maxPrice: Double = 1e6,
                                maxQuantity: Int = 10000): FillLike = {
    val price = generateRandomPrice()
    val quantity = generateRandomQuantity()

    PartialFill(askTradingPartyRef, bidTradingPartyRef, instrument, price, quantity)
  }

  def generateRandomTotalFill(askTradingPartyRef: ActorRef,
                              bidTradingPartyRef: ActorRef,
                              instrument: Stock,
                              maxPrice: Double = 1e6,
                              maxQuantity: Int = 10000): FillLike = {

    val price = generateRandomPrice()
    val quantity = generateRandomQuantity()

    TotalFill(askTradingPartyRef, bidTradingPartyRef, instrument, price, quantity)

  }

  def generateRandomPrice(maxPrice: Double = 1000.0): Double = {
    Random.nextDouble() * maxPrice
  }

  def generateRandomQuantity(maxQuantity: Int = 10000): Int = {
    Random.nextInt(maxQuantity)
  }

  feature("BilateralClearingMechanism should process transactions.") {

    val testInstrument = Stock("APPL")
    val clearingMechanism = TestActorRef(Props[BilateralClearingMechanism])

    scenario("BilateralClearingMechanism receives a PartialFill.") {

      val askTradingParty = TestProbe()
      val bidTradingParty = TestProbe()
      val fill = generateRandomPartialFill(askTradingParty.ref, bidTradingParty.ref, testInstrument)

      When("BilateralClearingMechanism receives a FillLike")

      clearingMechanism ! fill

      Then("AskTradingParty should receive a request for Securities")

      val securitiesRequest = AssetsRequest(fill.instrument, fill.quantity)
      askTradingParty.expectMsg(securitiesRequest)

      Then("BidTradingParty should receive a request for Payment")

      val paymentRequest = PaymentRequest(fill.price * fill.quantity)
      bidTradingParty.expectMsg(paymentRequest)

    }


    scenario("BilateralClearingMechanism receives a TotalFill.") {

      val askTradingParty = TestProbe()
      val bidTradingParty = TestProbe()
      val fill = generateRandomTotalFill(askTradingParty.ref, bidTradingParty.ref, testInstrument)

      When("BilateralClearingMechanism receives a FillLike")

      clearingMechanism ! fill

      Then("AskTradingParty should receive a request for Securities")

      val securitiesRequest = AssetsRequest(fill.instrument, fill.quantity)
      askTradingParty.expectMsg(securitiesRequest)

      Then("BidTradingParty should receive a request for Payment")

      val paymentRequest = PaymentRequest(fill.price * fill.quantity)
      bidTradingParty.expectMsg(paymentRequest)

    }

  }

}