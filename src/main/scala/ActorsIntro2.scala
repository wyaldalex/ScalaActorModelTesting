import akka.actor.{Actor, ActorSystem, Props, ActorRef}

object ActorsIntro2 extends App {

  val actorSystem = ActorSystem("xActorSystem")

  case class SpecialBuisnessClass(commaMessage: String)
  case class SayHiTo(ref: ActorRef)
  case class WirelessPhoneMessage(content: String,ref: ActorRef)

  class SimpleActor extends Actor {
    override def receive: Receive = {
      case message : String => println(s"[$self] I have received a message $message")
      case number : Int  => println(s"I have received a number $number")
      case inputClass1 : SpecialBuisnessClass =>   println(s"I have received a special class ${inputClass1.commaMessage}")
      case SayHiTo(ref) => ref ! 12121
      case WirelessPhoneMessage(xcontent, xref) => xref forward  (xcontent + " T1000")
    }
  }

  val simpleActor = actorSystem.actorOf(Props[SimpleActor], "simpleActor1")
  val bob = actorSystem.actorOf(Props[SimpleActor], "bob")
  val alice = actorSystem.actorOf(Props[SimpleActor], "alice")
  simpleActor ! SpecialBuisnessClass("12121, Albert, Accounting")

  bob ! SayHiTo(alice)
  bob ! WirelessPhoneMessage("Some message ", alice)

  //Crazy AKKA multi actor chaining







}
