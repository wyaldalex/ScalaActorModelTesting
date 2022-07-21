import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import com.typesafe.config.ConfigFactory

object ActorLogginTesting extends App {

  //If no config is specified it takes whatever is in resources/application.conf
  val actorSystem = ActorSystem()

  class SimpleActor extends Actor with ActorLogging {
    override def receive: Receive = {
      case msg : String => {
        log.info(s"I receive the message $msg")

      }
    }
  }

  val simpleActor = actorSystem.actorOf(Props[SimpleActor],"simpleActor")
  simpleActor ! "Some random message"

  //Testing configuration
  val defaultActorSystem = ActorSystem("DefaultConfigFileDemo")
  val defaultConfigActor = defaultActorSystem.actorOf(Props[SimpleActor], "defaultConfigActor")
  defaultConfigActor ! "Some Other message for default config actor"

  //Should not log anything as the logger level is ERROR
  val secretConfigOtherFile = ConfigFactory.load("secretConfigs/secretConfig.conf")
  val secretConfigSystem = ActorSystem("SecretConfigSystem", secretConfigOtherFile)
  val secretConfigActor = secretConfigSystem.actorOf(Props[SimpleActor], "secretConfigActor")
  secretConfigActor ! "Message should never be logged"









}
