package testing

import akka.actor.SupervisorStrategy.{Escalate, Restart, Resume, Stop}
import akka.actor.{Actor, ActorRef, ActorSystem, OneForOneStrategy, Props, Terminated}
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}

class SupervisionSpec extends TestKit(ActorSystem("supervisorSystem"))
  with WordSpecLike
  with BeforeAndAfterAll
  with ImplicitSender {

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  import SupervisionSpec._
  "A supervisor " should {
    "resume its child case of a minor fault" in {
      val supervisor = system.actorOf(Props[Supervisor])
      supervisor ! Props[FussyWordCounter]
      val child = expectMsgType[ActorRef]

      child ! "I love Akka"
      child ! Report
      expectMsg(3)

      child ! "This is a very long string with more than 20 words which should make the actor fail but also be able to recover"
      child ! Report
      expectMsg(3)
    }

    "restart its child in case of an empty sentence" in {
      val supervisor = system.actorOf(Props[Supervisor])
      supervisor ! Props[FussyWordCounter]
      val child = expectMsgType[ActorRef]

      child ! "I love Akka"
      child ! Report
      expectMsg(3)

      //Thread.sleep(1000)
      child ! ""
      child ! Report
      expectMsg(0)

    }
    "terminate its child in case of a major error" in {
      val supervisor = system.actorOf(Props[Supervisor])
      supervisor ! Props[FussyWordCounter]
      val child = expectMsgType[ActorRef]

      watch(child)
      child ! "akka is nice"
      val terminatedMessage = expectMsgType[Terminated]
      assert(terminatedMessage.actor == child)
    }

    "escalate error if not string" in {
      val supervisor = system.actorOf(Props[Supervisor])
      supervisor ! Props[FussyWordCounter]
      val child = expectMsgType[ActorRef]

      watch(child)
      child ! 43
      val terminatedMessage = expectMsgType[Terminated]
      assert(terminatedMessage.actor == child)
    }

  }



}

object SupervisionSpec {

  class Supervisor extends Actor {

    override val supervisorStrategy = OneForOneStrategy() {
      case _: NullPointerException => Restart
      case _: IllegalArgumentException => Stop
      case _: RuntimeException => Resume
      case _: Exception => Escalate
    }

    override def receive: Receive = {
      case props : Props => {
        val childRef = context.actorOf(props)
        sender() ! childRef
      }
    }
  }

  case object Report
  class FussyWordCounter extends Actor {
    var words = 0

    override def receive: Receive = {
      case Report => sender() ! words
      case "" => throw new NullPointerException("sentence is empty")
      case sentence: String => {
        if (sentence.length > 20 ) throw new RuntimeException("Sentence is too long")
        else if(!Character.isUpperCase(sentence(0))) throw new IllegalArgumentException("First Character is not uppercase")
        else words += sentence.split(" ").length
      }
      case _ => throw new Exception("can only receive strings")
    }
  }

}


