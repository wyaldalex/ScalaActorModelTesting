import akka.actor.{Actor, ActorLogging, ActorSystem, Props}

import scala.util.Random

object DispatcherTesting extends App {

  class Counter extends Actor with ActorLogging  {
    var count = 0
    override def receive: Receive = {
      case message => {
        count += 1
        log.info(s"current counter value $count with message $message")
      }
    }
  }

  //Requieres configuration setup
  /*
  #dispatchers
my-dispatcher {
  type = Dispatcher
  executor = "thread-pool-executor"
  thread-pool-executor {
    fixed-pool-size = 3
  }
  throughput = 30
}
   */
  val system = ActorSystem("DispatcherDemo")
  val actors = for (i <- 1 to 10) yield {
    system.actorOf(Props[Counter].withDispatcher("my-dispatcher"), s"counter_$i")
  }
  val r = new Random()
  for (i <- 1 to 1000){
    actors(r.nextInt(10)) ! i
  }



}
