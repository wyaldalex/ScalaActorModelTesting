package testing

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}

//The way I see it, TestProbes are a way of Mocking sub actor dependencies
//Similar to how Beans can be mocked with Mockit?
class TestProbes extends TestKit(ActorSystem("systemTestProbe"))
  with ImplicitSender
  with WordSpecLike
  with BeforeAndAfterAll {

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  import TestProbeSpec._

  "A master should" should {
    "register a slave" in {
      val master = system.actorOf(Props[Master])
      val slave  = TestProbe("slave")

      master ! Register(slave.ref)
      expectMsg(RegistrationAck)
    }
    "send work to the slave" in {
      val master = system.actorOf(Props[Master])
      val slave  = TestProbe("slave")

      master ! Register(slave.ref)
      expectMsg(RegistrationAck)

      val workLoadString = "Science Vessel AKKA"
      master ! Work(workLoadString)
      //testActor, some sort of implicit actor created by the testkit framework that receives messages
      slave.expectMsg(SlaveWork(workLoadString, testActor))
      //Mock the slave response
      slave.reply(WorkCompleted(3,testActor))
      expectMsg(Report(3))
    }

    "aggregate data correctly" in {
      val master = system.actorOf(Props[Master])
      val slave  = TestProbe("slave")
      master ! Register(slave.ref)
      expectMsg(RegistrationAck)

      val workLoadString = "Science Vessel AKKA"
      master ! Work(workLoadString)
      master ! Work(workLoadString)
      master ! Work(workLoadString)

      //Always that message is sent to the mocked actor, reply with WorkCompleted(3,testActor))
      slave.receiveWhile() {
        case SlaveWork(`workLoadString`, `testActor`) => slave.reply(WorkCompleted(3,testActor))
      }
      expectMsg(Report(3))
      expectMsg(Report(6))
      expectMsg(Report(9))

    }



  }

}

object TestProbeSpec {

  case class Work(text: String)
  case class SlaveWork(text: String, originalRequester: ActorRef)
  case class WorkCompleted(count: Int, originalRequester: ActorRef)
  case class Register(slaveRef: ActorRef)
  case class Report(totalCount: Int)
  case object RegistrationAck

  class Master extends Actor {
    override def receive: Receive = {
      case Register(slaveRef) => {
        sender() ! RegistrationAck
        context.become(online(slaveRef, 0))
      }
    }

    def online(slaveRef: ActorRef, totalWordCount: Int) : Receive = {
      case Work(text) => slaveRef ! SlaveWork(text, sender())
      case WorkCompleted(count, originalRequester) =>
        val newTotalWordCount = totalWordCount + count
        originalRequester ! Report(newTotalWordCount)
        context.become(online(slaveRef, newTotalWordCount))

    }

  }

}
