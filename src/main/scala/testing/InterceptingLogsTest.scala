package testing

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.testkit.{EventFilter, ImplicitSender, TestKit, TestProbe}
import com.typesafe.config.ConfigFactory
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}

class InterceptingLogsTest extends TestKit(ActorSystem("systemTimedAssertions", ConfigFactory.load().getConfig("interceptingLogMessages")))
with ImplicitSender
with WordSpecLike
with BeforeAndAfterAll {

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  import InterceptingLogsSpec._

  val item = "Rock the JVM Akka course"
  val creditCard = "1234-1234-1234-1234"

  "A checkout flow" should {
    "correctly log the dispatch of an order" in {
      EventFilter.info(pattern = s"Order [0-9]+ for item $item has been dispatched") intercept {
        val checkoutRef = system.actorOf(Props[CheckoutActor])
        checkoutRef ! Checkout(item, creditCard)
      }
    }
  }

}

object InterceptingLogsSpec {

  case class Checkout(item: String, creditCard: String)
  case class AuthorizedCard(creditCard: String)
  case object PaymentAccepted
  case object PaymentDenied
  case class DispatchOrder(item: String)
  case object OrderConfirmed

  class CheckoutActor extends Actor {
    private val paymentManager = context.actorOf(Props[PaymentManager])
    private val fulfillmentManager = context.actorOf(Props[FulfillmentManager])

    override def receive: Receive = awaitingCheckout

    def awaitingCheckout : Receive = {
      case Checkout(item,card) => {
        paymentManager ! AuthorizedCard(card)
        context.become(pendingPayment(item))
      }
    }
    def pendingPayment(item: String) : Receive = {
      case PaymentAccepted => {
        fulfillmentManager ! DispatchOrder(item)
        context.become(pendingFulfillment(item))
      }
    }
    def pendingFulfillment(item: String) : Receive = {
      case OrderConfirmed => context.become(awaitingCheckout)
    }
  }

  class PaymentManager extends Actor {
    override def receive: Receive = {
      case AuthorizedCard(card) => {
        if(card.startsWith("0")) sender() ! PaymentDenied
        else sender() ! PaymentAccepted
      }
    }
  }

  class FulfillmentManager extends Actor with ActorLogging {
    var orderId = 43
    override def receive: Receive = {
      case DispatchOrder(item) => {
        orderId += 1
        log.info(s"Order $orderId for item $item has been dispatched")
        sender() ! OrderConfirmed
      }
    }
  }

}
