import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Cancellable, Props, Timers}
import akka.routing.{Broadcast, RoundRobinPool}

object RoutersTesting extends App {

  class Worker extends Actor with ActorLogging {
    override def receive: Receive = {
      case message => log.info(s"I received some message ${message.toString}")
    }
  }

  val system = ActorSystem("SchedulerTimersDemo")
  val poolMaster = system.actorOf(RoundRobinPool(10).props(Props[Worker]), "simplePoolMaster")
  for(i <- 1 to 100){
    poolMaster ! s"number $i of some message"
  }

  poolMaster ! Broadcast("Broadcast message to all actors")


}
