import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.dispatch.{PriorityGenerator, UnboundedPriorityMailbox}
import com.typesafe.config.Config

object PriorityMailBoxTest extends  App {

  val system = ActorSystem("systemMailbox")

  class SimpleActor extends Actor with ActorLogging {
    override def receive: Receive = {
      case message => log.info(message.toString)
    }
  }

  class SupportTickerPriorityMailbox(settings: ActorSystem.Settings, config: Config) extends UnboundedPriorityMailbox(
    PriorityGenerator {
      case message: String if message.startsWith("[P0]") => 0
      case message: String if message.startsWith("[P1]") => 1
      case message: String if message.startsWith("[P2]") => 2
      case message: String if message.startsWith("[P3]") => 3
      case message: String if message.startsWith("[P4]") => 4
      case _ => 5
    })

  val supportTicketLogger = system.actorOf(Props[SimpleActor].withDispatcher("support-ticket-dispatcher"))
  supportTicketLogger ! "[P4] Low priority message"
  supportTicketLogger ! "[P4] Low priority message"
  supportTicketLogger ! "[P4] Low priority message"
  supportTicketLogger ! "[P4] Low priority message"
  supportTicketLogger ! "[P3] Normal priority message"
  supportTicketLogger ! "[P3] Normal priority message"
  supportTicketLogger ! "[P0] Super High priority message"
  supportTicketLogger ! "[P2] High priority message"
  supportTicketLogger ! "[P2] High priority message"
  supportTicketLogger ! "[P2] High priority message"
  supportTicketLogger ! "[P2] High priority message"
  supportTicketLogger ! "[P0] Super High priority message"



}
