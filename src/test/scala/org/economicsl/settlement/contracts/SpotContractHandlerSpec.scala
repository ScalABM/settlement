package org.economicsl.settlement.contracts

import akka.actor.ActorSystem
import akka.testkit.{TestActorRef, TestKit, TestProbe}
import org.economicsl.settlement._
import org.economicsl.settlement.actors.SpotContractHandler
import org.scalatest.{BeforeAndAfterAll, FeatureSpecLike, GivenWhenThen, Matchers}

import scala.util.{Failure, Success}

class SpotContractHandlerSpec extends TestKit(ActorSystem("SpotContractHandlerSpec"))
  with FeatureSpecLike
  with GivenWhenThen
  with Matchers
  with BeforeAndAfterAll {

  /** Shutdown actor system when finished. */
  override def afterAll(): Unit = {
    system.terminate()
  }


  feature("SpotContractHandler should be able to settle SpotContracts.") {

    scenario("SpotContractHandler receives a PartialFill.") {

      Given("An existing PartialFill")

      val askTradingParty = TestProbe()
      val bidTradingParty = TestProbe()
      val contract: SpotContract = ???

      When("SpotContractHandler receives a PartialFill")

      val transactionHandlerRef = TestActorRef(new SpotContractHandler(contract))

      Then("SpotContractHandler should send requests for payment and securities.")

      val securitiesRequest = RequestAsset.from(contract)
      askTradingParty.expectMsg(securitiesRequest)

      val paymentRequest = RequestPayment.from(contract)
      bidTradingParty.expectMsg(paymentRequest)

    }

    scenario("SpotContractHandler receives a TotalFill.") {

      Given("An existing TotalFill")

      val askTradingParty = TestProbe()
      val bidTradingParty = TestProbe()
      val contract: SpotContract = ???

      When("SpotContractHandler receives the TotalFill")

      val transactionHandlerRef = TestActorRef(new SpotContractHandler(contract))

      Then("SpotContractHandler should send requests for payment and securities.")

      val securitiesRequest = RequestAsset.from(contract)
      askTradingParty.expectMsg(securitiesRequest)

      val paymentRequest = RequestPayment.from(contract)
      bidTradingParty.expectMsg(paymentRequest)

    }
  }

  feature("SpotContractHandler should receive Payment and Securities.") {

    scenario("SpotContractHandler receives Payment before Securities.") {

      Given("SpotContractHandler has already received a contract")

      val askTradingParty = TestProbe()
      val bidTradingParty = TestProbe()
      val contract: SpotContract = ???

      val transactionHandlerRef = TestActorRef(new SpotContractHandler(contract))

      val securitiesRequest = RequestAsset.from(contract)
      askTradingParty.expectMsg(securitiesRequest)

      val paymentRequest = RequestPayment.from(contract)
      bidTradingParty.expectMsg(paymentRequest)

      When("SpotContractHandler receives Payment")

      val payment = Payment(contract.price * contract.quantity)
      transactionHandlerRef ! Success(payment)

      When("SpotContractHandler receives Securities")

      val securities = Assets(contract.tradable, contract.quantity)
      transactionHandlerRef ! Success(securities)

      Then("SpotContractHandler should forward Payment to the seller")

      askTradingParty.expectMsg(payment)

      Then("SpotContractHandler should forward Securities to the buyer")

      bidTradingParty.expectMsg(securities)

    }

    scenario("SpotContractHandler receives Securities before Payment.") {

      Given("SpotContractHandler has already received a contract")

      val askTradingParty = TestProbe()
      val bidTradingParty = TestProbe()
      val contract: SpotContract = ???

      val transactionHandlerRef = TestActorRef(new SpotContractHandler(contract))

      val securitiesRequest = RequestAsset.from(contract)
      askTradingParty.expectMsg(securitiesRequest)

      val paymentRequest = RequestPayment.from(contract)
      bidTradingParty.expectMsg(paymentRequest)

      When("SpotContractHandler receives Securities")

      val securities = Assets(contract.instrument, contract.quantity)
      transactionHandlerRef ! Success(securities)

      When("SpotContractHandler receives Payment")

      val payment = Payment(contract.price * contract.quantity)
      transactionHandlerRef ! Success(payment)

      Then("SpotContractHandler should forward Payment to the seller")

      askTradingParty.expectMsg(payment)

      Then("SpotContractHandler should forward Securities to the buyer")

      bidTradingParty.expectMsg(securities)

    }

    scenario("SpotContractHandler receives InsufficientFundsException before Securities.") {

      Given("SpotContractHandler has already received a contract")

      val askTradingParty = TestProbe()
      val bidTradingParty = TestProbe()
      val contract: SpotContract = ???

      val transactionHandlerRef = TestActorRef(new SpotContractHandler(contract))

      val securitiesRequest = RequestAsset.from(contract)
      askTradingParty.expectMsg(securitiesRequest)

      val paymentRequest = RequestPayment.from(contract)
      bidTradingParty.expectMsg(paymentRequest)

      When("SpotContractHandler receives InsufficientFundsException")

      transactionHandlerRef ! Failure(InsufficientFundsException())

      When("SpotContractHandler receives Securities")

      val securities = Assets(contract.instrument, contract.quantity)
      transactionHandlerRef ! Success(securities)

      Then("SpotContractHandler should refund securities to the seller")

      askTradingParty.expectMsg(securities)
      bidTradingParty.expectNoMsg()

    }

    scenario("SpotContractHandler receives Securities before InsufficientFundsException.") {

      Given("SpotContractHandler has already received a contract")

      val askTradingParty = TestProbe()
      val bidTradingParty = TestProbe()
      val contract: SpotContract = ???

      val transactionHandlerRef = TestActorRef(new SpotContractHandler(contract))

      val securitiesRequest = RequestAsset.from(contract)
      askTradingParty.expectMsg(securitiesRequest)

      val paymentRequest = RequestPayment.from(contract)
      bidTradingParty.expectMsg(paymentRequest)

      When("SpotContractHandler receives Securities")

      val securities = Assets(contract.instrument, contract.quantity)
      transactionHandlerRef ! Success(securities)

      When("SpotContractHandler receives InsufficientFundsException")

      transactionHandlerRef ! Failure(InsufficientFundsException())

      Then("SpotContractHandler should refund securities to the seller")

      askTradingParty.expectMsg(securities)
      bidTradingParty.expectNoMsg()

    }


    scenario("SpotContractHandler receives InsufficientAssetsException before Payment.") {

      Given("SpotContractHandler has already received a contract")

      val askTradingParty = TestProbe()
      val bidTradingParty = TestProbe()
      val contract: SpotContract = ???

      val transactionHandlerRef = TestActorRef(new SpotContractHandler(contract))

      val securitiesRequest = RequestAsset.from(contract)
      askTradingParty.expectMsg(securitiesRequest)

      val paymentRequest = RequestPayment.from(contract)
      bidTradingParty.expectMsg(paymentRequest)

      When("SpotContractHandler receives InsufficientAssetsException")

      transactionHandlerRef ! Failure(InsufficientAssetsException())

      When("SpotContractHandler receives Payment")

      val payment = Payment(contract.price * contract.quantity)
      transactionHandlerRef ! Success(payment)

      Then("SpotContractHandler should refund payment to the buyer")

      askTradingParty.expectNoMsg()
      bidTradingParty.expectMsg(payment)

    }

    scenario("SpotContractHandler receives Payment before InsufficientAssetsException.") {

      Given("SpotContractHandler has already received a contract")

      val askTradingParty = TestProbe()
      val bidTradingParty = TestProbe()
      val contract: SpotContract = ???

      val transactionHandlerRef = TestActorRef(new SpotContractHandler(contract))

      val securitiesRequest = RequestAsset.from(contract)
      askTradingParty.expectMsg(securitiesRequest)

      val paymentRequest = RequestPayment.from(contract)
      bidTradingParty.expectMsg(paymentRequest)

      When("SpotContractHandler receives Payment")

      val payment = Payment(contract.price * contract.quantity)
      transactionHandlerRef ! Success(payment)

      When("SpotContractHandler receives InsufficientAssetsException")

      transactionHandlerRef ! Failure(InsufficientAssetsException())

      Then("SpotContractHandler should refund payment to the buyer")

      askTradingParty.expectNoMsg()
      bidTradingParty.expectMsg(payment)

    }

    scenario("SpotContractHandler receives InsufficientFundsException before InsufficientAssetsException.") {

      Given("SpotContractHandler has already received a contract")

      val askTradingParty = TestProbe()
      val bidTradingParty = TestProbe()
      val contract: SpotContract = ???

      val transactionHandlerRef = TestActorRef(new SpotContractHandler(contract))

      val securitiesRequest = RequestAsset.from(contract)
      askTradingParty.expectMsg(securitiesRequest)

      val paymentRequest = RequestPayment.from(contract)
      bidTradingParty.expectMsg(paymentRequest)

      When("SpotContractHandler receives InsufficientFundsException")

      transactionHandlerRef ! Failure(InsufficientFundsException())

      When("SpotContractHandler receives InsufficientAssetsException")

      transactionHandlerRef ! Failure(InsufficientAssetsException())

      Then("SpotContractHandler should not send any messages.")

      askTradingParty.expectNoMsg()
      bidTradingParty.expectNoMsg()

    }

  }

}