package org.economicsl.settlement

import akka.actor.ActorSystem
import akka.testkit.{TestActorRef, TestKit, TestProbe}
import org.economicsl.settlement.contracts.SpotContract
import org.scalatest.{BeforeAndAfterAll, FeatureSpecLike, GivenWhenThen, Matchers}

import scala.util.Success


class CCPClearingMechanismSpec extends TestKit(ActorSystem("CCPClearingMechanismSpec"))
  with FeatureSpecLike
  with GivenWhenThen
  with Matchers
  with BeforeAndAfterAll {

  /** Shutdown actor system when finished. */
  override def afterAll(): Unit = {
    system.terminate()
  }

  feature("CCPClearingMechanism should process transactions.") {

    val testInstrument = Stock("APPL")

    val clearingMechanismRef = TestActorRef(new CCPSettlementMechanism)
    val clearingMechanism = clearingMechanismRef.underlyingActor

    val askTradingParty = TestProbe()
    val bidTradingParty = TestProbe()

    scenario("CCPClearingMechanism receives a PartialFill.") {

      val contract: SpotContract = ???

      // store initial holdings of cash and securities
      val clearingMechanismInitialSecurities = clearingMechanism.holdings(testInstrument)
      val clearingMechanismInitialCash = clearingMechanism.holdings(Currency)

      When("CCPClearingMechanism receives a PartialFill")

      clearingMechanismRef ! contract

      Then("AskTradingParty should receive a request for Securities")

      askTradingParty.expectMsg(AssetsRequest.from(contract))
      askTradingParty.reply(Success(Assets(contract.instrument, contract.quantity)))

      Then("BidTradingParty should receive a request for Payment")

      val paymentAmount = contract.price * contract.quantity
      bidTradingParty.expectMsg(PaymentRequest(paymentAmount))
      bidTradingParty.reply(Success(Payment(paymentAmount)))

      Then("AskTradingParty should receive a Payment")

      askTradingParty.expectMsg(Payment(paymentAmount))

      Then("BidTradingParty should receive a Securities")

      bidTradingParty.expectMsg(Assets(contract.instrument, contract.quantity))

      Then("CCPClearingMechanism securities holdings should remain unchanged.")

      clearingMechanism.holdings(testInstrument) should be(clearingMechanismInitialSecurities)

      Then("CCPClearingMechanism cash holdings should remain unchanged.")

      clearingMechanism.holdings(Currency) should be(clearingMechanismInitialCash)

    }

    scenario("CCPClearingMechanism receives a TotalFill.") {

      val contract = generateRandomTotalFill(askTradingParty.ref, bidTradingParty.ref, testInstrument)

      // store initial holdings of cash and securities
      val clearingMechanismInitialSecurities = clearingMechanism.holdings(testInstrument)
      val clearingMechanismInitialCash = clearingMechanism.holdings(Currency)

      When("CCPClearingMechanism receives a PartialFill")

      clearingMechanismRef ! contract

      Then("AskTradingParty should receive a request for Securities")

      askTradingParty.expectMsg(AssetsRequest.from(contract))
      askTradingParty.reply(Success(Assets(contract.instrument, contract.quantity)))

      Then("BidTradingParty should receive a request for Payment")

      val paymentAmount = contract.price * contract.quantity
      bidTradingParty.expectMsg(PaymentRequest(paymentAmount))
      bidTradingParty.reply(Success(Payment(paymentAmount)))

      Then("AskTradingParty should receive a Payment")

      askTradingParty.expectMsg(Payment(paymentAmount))

      Then("BidTradingParty should receive a Securities")

      bidTradingParty.expectMsg(Assets(contract.instrument, contract.quantity))

      Then("CCPClearingMechanism securities holdings should remain unchanged.")

      clearingMechanism.holdings(testInstrument) should be(clearingMechanismInitialSecurities)

      Then("CCPClearingMechanism cash holdings should remain unchanged.")

      clearingMechanism.holdings(Currency) should be(clearingMechanismInitialCash)

    }

  }

}