package org.economicsl.settlement

import akka.actor.{ActorRef, ActorSystem}
import akka.testkit.{TestActorRef, TestProbe, TestKit}
import models._
import org.scalatest.{BeforeAndAfterAll, Matchers, GivenWhenThen, FeatureSpecLike}

import scala.util.{Success, Random}


class CCPClearingMechanismSpec
  extends TestKit(ActorSystem("CCPClearingMechanismSpec"))
  with FeatureSpecLike
  with GivenWhenThen
  with Matchers
  with BeforeAndAfterAll {

  /** Shutdown actor system when finished. */
  override def afterAll(): Unit = {
    system.terminate()
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


  feature("CCPClearingMechanism should process transactions.") {

    val testInstrument = Stock("APPL")

    val clearingMechanismRef = TestActorRef(new CCPSettlementMechanism)
    val clearingMechanism = clearingMechanismRef.underlyingActor

    val askTradingParty = TestProbe()
    val bidTradingParty = TestProbe()

    scenario("CCPClearingMechanism receives a PartialFill.") {

      val fill = generateRandomPartialFill(askTradingParty.ref, bidTradingParty.ref, testInstrument)

      // store initial holdings of cash and securities
      val clearingMechanismInitialSecurities = clearingMechanism.assets(testInstrument)
      val clearingMechanismInitialCash = clearingMechanism.assets(Currency)

      When("CCPClearingMechanism receives a PartialFill")

      clearingMechanismRef ! fill

      Then("AskTradingParty should receive a request for Securities")

      askTradingParty.expectMsg(AssetsRequest(fill.instrument, fill.quantity))
      askTradingParty.reply(Success(Assets(fill.instrument, fill.quantity)))

      Then("BidTradingParty should receive a request for Payment")

      val paymentAmount = fill.price * fill.quantity
      bidTradingParty.expectMsg(PaymentRequest(paymentAmount))
      bidTradingParty.reply(Success(Payment(paymentAmount)))

      Then("AskTradingParty should receive a Payment")

      askTradingParty.expectMsg(Payment(paymentAmount))

      Then("BidTradingParty should receive a Securities")

      bidTradingParty.expectMsg(Assets(fill.instrument, fill.quantity))

      Then("CCPClearingMechanism securities holdings should remain unchanged.")

      clearingMechanism.assets(testInstrument) should be(clearingMechanismInitialSecurities)

      Then("CCPClearingMechanism cash holdings should remain unchanged.")

      clearingMechanism.assets(Currency) should be(clearingMechanismInitialCash)

    }

    scenario("CCPClearingMechanism receives a TotalFill.") {

      val fill = generateRandomTotalFill(askTradingParty.ref, bidTradingParty.ref, testInstrument)

      // store initial holdings of cash and securities
      val clearingMechanismInitialSecurities = clearingMechanism.assets(testInstrument)
      val clearingMechanismInitialCash = clearingMechanism.assets(Currency)

      When("CCPClearingMechanism receives a PartialFill")

      clearingMechanismRef ! fill

      Then("AskTradingParty should receive a request for Securities")

      askTradingParty.expectMsg(AssetsRequest(fill.instrument, fill.quantity))
      askTradingParty.reply(Success(Assets(fill.instrument, fill.quantity)))

      Then("BidTradingParty should receive a request for Payment")

      val paymentAmount = fill.price * fill.quantity
      bidTradingParty.expectMsg(PaymentRequest(paymentAmount))
      bidTradingParty.reply(Success(Payment(paymentAmount)))

      Then("AskTradingParty should receive a Payment")

      askTradingParty.expectMsg(Payment(paymentAmount))

      Then("BidTradingParty should receive a Securities")

      bidTradingParty.expectMsg(Assets(fill.instrument, fill.quantity))

      Then("CCPClearingMechanism securities holdings should remain unchanged.")

      clearingMechanism.assets(testInstrument) should be(clearingMechanismInitialSecurities)

      Then("CCPClearingMechanism cash holdings should remain unchanged.")

      clearingMechanism.assets(Currency) should be(clearingMechanismInitialCash)

    }

  }

}