package testing

import akka.actor.{Actor, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import com.typesafe.config.ConfigFactory
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}

import scala.concurrent.duration._
import scala.util.Random

//To be able to use the seconds and millis stuff
import scala.language.postfixOps

class TimedAssertionsTest extends TestKit(ActorSystem("systemTimedAssertions", ConfigFactory.load().getConfig("specialTimedAssertionConfig")))
  with ImplicitSender
  with WordSpecLike
  with BeforeAndAfterAll {

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  import TimedAssertionsSpec._
  "a worker actor " should {
    val workerActor = system.actorOf(Props[WorkerActor])

    "reply with meaning of life" in {
      within(500 millis, 1 second) {
        workerActor ! "work"
        expectMsg(WorkResult(42))
      }
    }

    "reply with valid work at a reasonable cadence " in {
      workerActor ! "workSequence"

      val results: Seq[Int] = receiveWhile[Int](max = 2 seconds, idle = 500 millis, messages = 10){
        case WorkResult(result) => result
      }

      assert(results.sum > 5)
    }

    "should not receive message with custom timed config 0.3 seconds" in {
      within(1 second) {
        val probe  = TestProbe()
        probe.send(workerActor,"work")
        //probe.expectMsg(WorkResult(42))
        expectNoMessage(200 millis)
      }
    }


  }


}

object TimedAssertionsSpec {

  case class WorkResult(result: Int)

  class WorkerActor extends Actor {
    override def receive: Receive = {
      case "work" => {
        //simulate long computation
        Thread.sleep(500)
        sender() ! WorkResult(42)
      }
      case "workSequence" => {
        val r = new Random()
        for (_ <- 1 to 10) {
          Thread.sleep(r.nextInt(50))
          sender() ! WorkResult(1)
        }
      }
    }
  }

}
