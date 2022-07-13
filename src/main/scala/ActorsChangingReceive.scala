import ActorsChangingReceive.FussyKid.{KidAccept, KidReject}
import ActorsChangingReceive.Mom.MomStart
import akka.actor.{Actor, ActorRef, ActorSystem, Props}

object ActorsChangingReceive  extends App{

/*
  def someFun(name : String): Int = {
    1313
  } */

  //Domain Object
  object FussyKid{
    case object KidAccept
    case object KidReject
    val HAPPY = "happy"
    val SAD = "sad"
  }
  class StatelessFussyKid extends Actor{
    import FussyKid._
    import Mom._

    //Set the default receive method
    override def receive: Receive = happyReceive

    def happyReceive: Receive = {
      case Food(VEGETABLE) => context.become(sadReceive)
      case Food(CHOCOLATE) =>
      case Ask(_) => sender() ! KidAccept

    }

    def sadReceive: Receive = {
      case Food(VEGETABLE) =>
      case Food(CHOCOLATE) => context.become(happyReceive)
      case Ask(_) => sender() ! KidReject


    }

  }


  object Mom {
    case class MomStart(kidRef: ActorRef)
    case class Food(food: String)
    case class Ask(message: String)
    val VEGETABLE = "veggies"
    val CHOCOLATE = "chocolate"
  }

  class Mom extends Actor {
    import Mom._

    override def receive: Receive = {
      case MomStart(kidRef) =>
        kidRef ! Food(VEGETABLE)
        kidRef ! Food(CHOCOLATE)
        kidRef ! Food(VEGETABLE)
        kidRef ! Ask("do you want to play?")
      case KidAccept => println("Yay, my kid is happy")
      case KidReject => println("Kid is sad, but at least its healthy")

    }

  }

  val actorSystem = ActorSystem("xActorSystem")
  val fussyKid = actorSystem.actorOf(Props[StatelessFussyKid], "fussyKidActor")
  val mom = actorSystem.actorOf(Props[Mom], "momActor")

  //Normal approach with complete override of receive
  mom ! MomStart(fussyKid)

}

