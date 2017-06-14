package org.economicsl.settlement

import akka.actor.{ActorRef, ActorSystem}
import akka.testkit.{TestActorRef, TestProbe, TestKit}
import models._
import org.scalatest.{BeforeAndAfterAll, Matchers, GivenWhenThen, FeatureSpecLike}

import scala.util.{Failure, Success, Random}

class TransactionHandlerSpec
  extends TestKit(ActorSystem("TransactionHandlerSpec"))
  with FeatureSpecLike
  with GivenWhenThen
  with Matchers
  with BeforeAndAfterAll {

  /** Shutdown actor system when finished. */
  override def afterAll(): Unit = {
    system.terminate()
  }

  def generateRandomAmount(maxAmount: Double = 1e6): Double = {
    Random.nextDouble() * maxAmount
  }

  def generateRandomPrice(maxPrice: Double = 1000.0): Double = {
    Random.nextDouble() * maxPrice
  }

  def generateRandomQuantity(maxQuantity: Int = 10000): Int = {
    Random.nextInt(maxQuantity)
  }

  def generateRandomInstrument(maxQuantity: Int = 10000): Stock = {
    Stock(Random.nextString(4))
  }

  def generateRandomPartialFill(askTradingPartyRef: ActorRef,
                                bidTradingPartyRef: ActorRef,
                                maxPrice: Double = 1e6,
                                maxQuantity: Int = 10000): PartialFill = {
    val instrument = generateRandomInstrument(maxQuantity)
    val price = generateRandomPrice(maxPrice)
    val quantity = generateRandomQuantity(maxQuantity)

    PartialFill(askTradingPartyRef, bidTradingPartyRef, instrument, price, quantity)
  }

  def generateRandomTotalFill(askTradingPartyRef: ActorRef,
                              bidTradingPartyRef: ActorRef,
                              maxPrice: Double = 1e6,
                              maxQuantity: Int = 10000): TotalFill = {

    val instrument = generateRandomInstrument(maxQuantity)
    val price = generateRandomPrice(maxPrice)
    val quantity = generateRandomQuantity(maxQuantity)

    TotalFill(askTradingPartyRef, bidTradingPartyRef, instrument, price, quantity)

  }

  feature("TransactionHandler should receive Fills.") {

    scenario("TransactionHandler receives a PartialFill.") {

      Given("An existing PartialFill")

      val askTradingParty = TestProbe()
      val bidTradingParty = TestProbe()
      val fill = generateRandomPartialFill(askTradingParty.ref, bidTradingParty.ref)

      When("TransactionHandler receives a PartialFill")

      val transactionHandlerRef = TestActorRef(new TransactionHandler(fill))

      Then("TransactionHandler should send requests for payment and securities.")

      val securitiesRequest = AssetsRequest(fill.instrument, fill.quantity)
      askTradingParty.expectMsg(securitiesRequest)

      val paymentRequest = PaymentRequest(fill.price * fill.quantity)
      bidTradingParty.expectMsg(paymentRequest)

    }

    scenario("TransactionHandler receives a TotalFill.") {

      Given("An existing TotalFill")

      val askTradingParty = TestProbe()
      val bidTradingParty = TestProbe()
      val fill = generateRandomTotalFill(askTradingParty.ref, bidTradingParty.ref)

      When("TransactionHandler receives the TotalFill")

      val transactionHandlerRef = TestActorRef(new TransactionHandler(fill))

      Then("TransactionHandler should send requests for payment and securities.")

      val securitiesRequest = AssetsRequest(fill.instrument, fill.quantity)
      askTradingParty.expectMsg(securitiesRequest)

      val paymentRequest = PaymentRequest(fill.price * fill.quantity)
      bidTradingParty.expectMsg(paymentRequest)

    }
  }

  feature("TransactionHandler should receive Payment and Securities.") {

    scenario("TransactionHandler receives Payment before Securities.") {

      Given("TransactionHandler has already received a fill")

      val askTradingParty = TestProbe()
      val bidTradingParty = TestProbe()
      val fill = generateRandomPartialFill(askTradingParty.ref, bidTradingParty.ref)

      val transactionHandlerRef = TestActorRef(new TransactionHandler(fill))

      val securitiesRequest = AssetsRequest(fill.instrument, fill.quantity)
      askTradingParty.expectMsg(securitiesRequest)

      val paymentRequest = PaymentRequest(fill.price * fill.quantity)
      bidTradingParty.expectMsg(paymentRequest)

      When("TransactionHandler receives Payment")

      val payment = Payment(fill.price * fill.quantity)
      transactionHandlerRef ! Success(payment)

      When("TransactionHandler receives Securities")

      val securities = Assets(fill.instrument, fill.quantity)
      transactionHandlerRef ! Success(securities)

      Then("TransactionHandler should forward Payment to the seller")

      askTradingParty.expectMsg(payment)

      Then("TransactionHandler should forward Securities to the buyer")

      bidTradingParty.expectMsg(securities)

    }

    scenario("TransactionHandler receives Securities before Payment.") {

      Given("TransactionHandler has already received a fill")

      val askTradingParty = TestProbe()
      val bidTradingParty = TestProbe()
      val fill = generateRandomPartialFill(askTradingParty.ref, bidTradingParty.ref)

      val transactionHandlerRef = TestActorRef(new TransactionHandler(fill))

      val securitiesRequest = AssetsRequest(fill.instrument, fill.quantity)
      askTradingParty.expectMsg(securitiesRequest)

      val paymentRequest = PaymentRequest(fill.price * fill.quantity)
      bidTradingParty.expectMsg(paymentRequest)

      When("TransactionHandler receives Securities")

      val securities = Assets(fill.instrument, fill.quantity)
      transactionHandlerRef ! Success(securities)

      When("TransactionHandler receives Payment")

      val payment = Payment(fill.price * fill.quantity)
      transactionHandlerRef ! Success(payment)

      Then("TransactionHandler should forward Payment to the seller")

      askTradingParty.expectMsg(payment)

      Then("TransactionHandler should forward Securities to the buyer")

      bidTradingParty.expectMsg(securities)

    }

    scenario("TransactionHandler receives InsufficientFundsException before Securities.") {

      Given("TransactionHandler has already received a fill")

      val askTradingParty = TestProbe()
      val bidTradingParty = TestProbe()
      val fill = generateRandomPartialFill(askTradingParty.ref, bidTradingParty.ref)

      val transactionHandlerRef = TestActorRef(new TransactionHandler(fill))

      val securitiesRequest = AssetsRequest(fill.instrument, fill.quantity)
      askTradingParty.expectMsg(securitiesRequest)

      val paymentRequest = PaymentRequest(fill.price * fill.quantity)
      bidTradingParty.expectMsg(paymentRequest)

      When("TransactionHandler receives InsufficientFundsException")

      transactionHandlerRef ! Failure(InsufficientFundsException())

      When("TransactionHandler receives Securities")

      val securities = Assets(fill.instrument, fill.quantity)
      transactionHandlerRef ! Success(securities)

      Then("TransactionHandler should refund securities to the seller")

      askTradingParty.expectMsg(securities)
      bidTradingParty.expectNoMsg()

    }

    scenario("TransactionHandler receives Securities before InsufficientFundsException.") {

      Given("TransactionHandler has already received a fill")

      val askTradingParty = TestProbe()
      val bidTradingParty = TestProbe()
      val fill = generateRandomPartialFill(askTradingParty.ref, bidTradingParty.ref)

      val transactionHandlerRef = TestActorRef(new TransactionHandler(fill))

      val securitiesRequest = AssetsRequest(fill.instrument, fill.quantity)
      askTradingParty.expectMsg(securitiesRequest)

      val paymentRequest = PaymentRequest(fill.price * fill.quantity)
      bidTradingParty.expectMsg(paymentRequest)

      When("TransactionHandler receives Securities")

      val securities = Assets(fill.instrument, fill.quantity)
      transactionHandlerRef ! Success(securities)

      When("TransactionHandler receives InsufficientFundsException")

      transactionHandlerRef ! Failure(InsufficientFundsException())

      Then("TransactionHandler should refund securities to the seller")

      askTradingParty.expectMsg(securities)
      bidTradingParty.expectNoMsg()

    }


    scenario("TransactionHandler receives InsufficientAssetsException before Payment.") {

      Given("TransactionHandler has already received a fill")

      val askTradingParty = TestProbe()
      val bidTradingParty = TestProbe()
      val fill = generateRandomPartialFill(askTradingParty.ref, bidTradingParty.ref)

      val transactionHandlerRef = TestActorRef(new TransactionHandler(fill))

      val securitiesRequest = AssetsRequest(fill.instrument, fill.quantity)
      askTradingParty.expectMsg(securitiesRequest)

      val paymentRequest = PaymentRequest(fill.price * fill.quantity)
      bidTradingParty.expectMsg(paymentRequest)

      When("TransactionHandler receives InsufficientAssetsException")

      transactionHandlerRef ! Failure(InsufficientAssetsException())

      When("TransactionHandler receives Payment")

      val payment = Payment(fill.price * fill.quantity)
      transactionHandlerRef ! Success(payment)

      Then("TransactionHandler should refund payment to the buyer")

      askTradingParty.expectNoMsg()
      bidTradingParty.expectMsg(payment)

    }

    scenario("TransactionHandler receives Payment before InsufficientAssetsException.") {

      Given("TransactionHandler has already received a fill")

      val askTradingParty = TestProbe()
      val bidTradingParty = TestProbe()
      val fill = generateRandomPartialFill(askTradingParty.ref, bidTradingParty.ref)

      val transactionHandlerRef = TestActorRef(new TransactionHandler(fill))

      val securitiesRequest = AssetsRequest(fill.instrument, fill.quantity)
      askTradingParty.expectMsg(securitiesRequest)

      val paymentRequest = PaymentRequest(fill.price * fill.quantity)
      bidTradingParty.expectMsg(paymentRequest)

      When("TransactionHandler receives Payment")

      val payment = Payment(fill.price * fill.quantity)
      transactionHandlerRef ! Success(payment)

      When("TransactionHandler receives InsufficientAssetsException")

      transactionHandlerRef ! Failure(InsufficientAssetsException())

      Then("TransactionHandler should refund payment to the buyer")

      askTradingParty.expectNoMsg()
      bidTradingParty.expectMsg(payment)

    }

    scenario("TransactionHandler receives InsufficientFundsException before InsufficientAssetsException.") {

      Given("TransactionHandler has already received a fill")

      val askTradingParty = TestProbe()
      val bidTradingParty = TestProbe()
      val fill = generateRandomPartialFill(askTradingParty.ref, bidTradingParty.ref)

      val transactionHandlerRef = TestActorRef(new TransactionHandler(fill))

      val securitiesRequest = AssetsRequest(fill.instrument, fill.quantity)
      askTradingParty.expectMsg(securitiesRequest)

      val paymentRequest = PaymentRequest(fill.price * fill.quantity)
      bidTradingParty.expectMsg(paymentRequest)

      When("TransactionHandler receives InsufficientFundsException")

      transactionHandlerRef ! Failure(InsufficientFundsException())

      When("TransactionHandler receives InsufficientAssetsException")

      transactionHandlerRef ! Failure(InsufficientAssetsException())

      Then("TransactionHandler should not send any messages.")

      askTradingParty.expectNoMsg()
      bidTradingParty.expectNoMsg()

    }

  }

}