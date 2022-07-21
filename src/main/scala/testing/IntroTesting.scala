package testing

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}

import scala.util.Random



class IntroTesting extends TestKit(ActorSystem("BasicSpec"))
  with ImplicitSender
  with WordSpecLike
  with BeforeAndAfterAll {

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  import BasicSpec._

  "A simple actor" should {
    "send back the same message" in {
      val echoActor = system.actorOf(Props[SimpleActor], "basicSpec")
      val message = "hello, test"
      echoActor ! message

      expectMsg(message)

    }
  }


  "A blackHole actor " should {
    "return nothing" in {
      val blackHoleActor = system.actorOf(Props[BlackHole])
      blackHoleActor ! "something"
      expectNoMessage()
    }
  }

  "A Lab actor " should {
    val labActor = system.actorOf(Props[LabTestActor])

    "return upper case" in {
      labActor ! "aaabbbccc xxxx"
      val reply = expectMsgType[String]

      assert(reply == "AAABBBCCC XXXX")
    }

    "reply to a greeting"  in {
      labActor ! "greeting"
      expectMsgAnyOf("hi", "hello")
    }

    "reply with favorite tech" in {
      labActor ! "favoriteTech"
      expectMsgAllOf("Scala", "Akka")
    }

    "different favorite tech assertion" in {
      labActor ! "favoriteTech"
      val messages = receiveN(2)
    }

    "fancy different favorite tech assertion" in {
      labActor ! "favoriteTech"

      expectMsgPF() {
        case "Scala" =>
        case "Akka" =>
      }

    }

  }

}

object BasicSpec {
  class SimpleActor extends Actor {
    override def receive: Receive = {
      case message => sender() ! message
    }
  }

  class BlackHole extends Actor {
    override def receive: Receive = Actor.emptyBehavior
  }

  class LabTestActor extends Actor {
    val random = new Random()

    override def receive: Receive = {
      case "greeting" =>
        if(random.nextBoolean()) sender() ! "hi" else sender() ! "hello"
      case "favoriteTech" =>
        sender() ! "Scala"
        sender() ! "Akka"
      case message: String => sender() ! message.toUpperCase()
    }
  }
}
