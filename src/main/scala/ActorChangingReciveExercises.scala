import ActorChangingReciveExercises.callerSystemActor.Start
import akka.actor.{Actor, ActorRef, ActorSystem, Props}

object ActorChangingReciveExercises extends App {


  case object Increment
  case object Decrement
  //case object Total(num : Int)
  //Counter withouth internal variable
  class StatelessCounter extends Actor {
    import callerSystemActor._
    override def receive: Receive = decrementReceive
    def decrementReceive: Receive = {
      case Increment => context.become(incrementReceive,false)
      case Decrement => context.unbecome()
      case Total(num) => {
        println(s"The total counter amount is $num")
        sender() ! Value(-1) //It means there is no more in the stack
      }
    }
    def incrementReceive: Receive = {
      case Increment => context.become(incrementReceive,false)
      case Decrement => context.unbecome()
      case Total(num) => {
        context.unbecome()
        sender() ! Value(num + 1)
      }
    }
  }

  case object callerSystemActor {
    case class Start(counterRef : ActorRef)
    case class Value(num : Int)
    case class Total(num : Int)
  }
  class callerSystemActor extends Actor {
    import callerSystemActor._
    override def receive: Receive = {
      case Start(counterRef) => {
        (1 to 5).foreach(_ => counter ! Increment)
        counterRef ! Total(0)
      }
      case Value(num) => {
        if(num != -1){
          println("Calling total again")
          sender() ! Total(num)
        } else {
          print(s"Counter value is $num")
        }
      }

    }

  }

  val actorSystem = ActorSystem("xActorSystem")
  val counter = actorSystem.actorOf(Props[StatelessCounter],"statelessCounter")
  val callerCounter = actorSystem.actorOf(Props[callerSystemActor],"callerSystemActorx")
  callerCounter ! Start(counter)



}
