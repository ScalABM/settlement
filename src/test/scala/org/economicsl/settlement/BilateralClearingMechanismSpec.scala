package org.economicsl.settlement

import akka.actor.{ActorSystem, Props}
import akka.testkit.{TestActorRef, TestKit, TestProbe}
import org.economicsl.settlement.actors.BilateralSettlementServiceActor
import org.economicsl.settlement.contracts.SpotContract
import org.scalatest.{BeforeAndAfterAll, FeatureSpecLike, GivenWhenThen, Matchers}


class BilateralClearingMechanismSpec extends TestKit(ActorSystem("NoiseTraderSpec"))
  with FeatureSpecLike
  with GivenWhenThen
  with Matchers
  with BeforeAndAfterAll {

  /** Shutdown actor system when finished. */
  override def afterAll(): Unit = {
    system.terminate()
  }

  feature("BilateralClearingMechanism should process transactions.") {

    val testInstrument = Stock("APPL")
    val clearingMechanism = TestActorRef(Props[BilateralSettlementServiceActor])

    scenario("BilateralClearingMechanism receives a PartialFill.") {

      val askTradingParty = TestProbe()
      val bidTradingParty = TestProbe()
      val contract: SpotContract = ???

      When("BilateralClearingMechanism receives a FillLike")

      clearingMechanism ! contract

      Then("AskTradingParty should receive a request for Securities")

      val securitiesRequest = RequestAsset.from(contract)
      askTradingParty.expectMsg(securitiesRequest)

      Then("BidTradingParty should receive a request for Payment")

      val paymentRequest = RequestPayment.from(contract)
      bidTradingParty.expectMsg(paymentRequest)

    }


    scenario("BilateralClearingMechanism receives a TotalFill.") {

      val askTradingParty = TestProbe()
      val bidTradingParty = TestProbe()
      val contract: SpotContract = ???

      When("BilateralClearingMechanism receives a FillLike")

      clearingMechanism ! contract

      Then("AskTradingParty should receive a request for Securities")

      val securitiesRequest = RequestAsset.from(contract)
      askTradingParty.expectMsg(securitiesRequest)

      Then("BidTradingParty should receive a request for Payment")

      val paymentRequest = RequestPayment.from(contract)
      bidTradingParty.expectMsg(paymentRequest)

    }

  }

}