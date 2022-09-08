import akka.actor.{Actor, ActorRef, ActorSystem, Props}

object ActorExercise1Remake  extends  App {

  val actorSystem = ActorSystem("testingActorSystem")

  object CounterActor {
    case object Increment
    case object Decrement
    case object Reset
    case object CurrentValue
  }
  class CounterActor extends Actor {
    import CounterActor._
    override def receive: Receive = counterReceive(0)

    def counterReceive(counter: Int): Receive = {
      case Increment => {
        context.become(counterReceive(counter + 1))
      }
      case CurrentValue => {
        println(s"Current counter value $counter")
      }
      case Reset => {
        context.become(counterReceive(0))
      }
      case Decrement => {
        context.become(counterReceive(counter - 1))
      }
    }
  }

  val counterActor = actorSystem.actorOf(Props[CounterActor])

  import CounterActor._
  counterActor ! Increment
  counterActor ! Increment
  counterActor ! Increment
  counterActor ! Increment
  counterActor ! Decrement
  counterActor ! Decrement
  counterActor ! CurrentValue



}
