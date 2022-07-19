import ActorChildrenTesting.Parent.{CreateChild, TellChild}
import akka.actor.{Actor, ActorRef, ActorSystem, Props}

object ActorChildrenTesting extends App{

  object Parent {
    case class CreateChild(name: String)
    case class TellChild(message: String)
  }
  class Parent extends Actor {
    import Parent._

    override def receive: Receive = {
      case CreateChild(name) =>
        println(s"${self.path} creating child")
        //create a new actor Here
        val childRef = context.actorOf(Props[Child], name)
        context.become(withChild(childRef))
    }
    def withChild(childRef: ActorRef) : Receive = {
      case TellChild(message) => childRef forward message
    }
  }

  class Child extends Actor {
    override def receive: Receive = {
      case message => println(s"${self.path}  I am a child I got this message : $message")
    }
  }

  val actorSystem = ActorSystem("xActorSystem")
  val parentActor = actorSystem.actorOf(Props[Parent], "parentActor")
  parentActor ! CreateChild("randomChild")
  parentActor ! TellChild("Are you alive child?")



}
