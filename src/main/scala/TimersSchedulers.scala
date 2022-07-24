import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Cancellable, Props, Timers}

import scala.concurrent.duration._
import scala.language.postfixOps

object TimersSchedulers extends App {

  class SimpleActor extends Actor with ActorLogging {

    override def receive: Receive = {
      case message => log.info(message.toString)
    }

  }

  val system = ActorSystem("SchedulerTimersDemo")
  val simpleActor = system.actorOf(Props[SimpleActor])
  system.log.info("Scheduling reminder for simple actor")

  import system.dispatcher
  system.scheduler.scheduleOnce(1 second) {
    simpleActor ! "reminder"
  }

  //depecrated way of scheduling
  val routine = system.scheduler.schedule(1 second, 2 seconds){
    simpleActor ! "heartbeat"
  }

  //new form?
  val routine2 =
    system.scheduler.scheduleWithFixedDelay(Duration.Zero, 50.milliseconds, simpleActor,"simpleActor2 hearbeat2")

  //Both get cancelled the same
  //routine.cancel()
  //routine2.cancel()

  //Change time depending on how long you want to keep them alive
  system.scheduler.scheduleOnce(10 millis) {
    routine.cancel()
    routine2.cancel()
  }

  //A self closing actor
  class SelfClosingActor extends  Actor with ActorLogging {
    var schedule = createTimeoutWindow

    def createTimeoutWindow(): Cancellable ={
      context.system.scheduler.scheduleOnce(1 second) {
        self ! "timeout"
      }
    }

    override def receive: Receive = {
      case "timeout" => {
        log.info("Stopping myself")
        context.stop(self)
      }
      case message => {
        log.info(s"Received $message, staying alive"  )
        schedule.cancel()
        schedule = createTimeoutWindow
      }
    }
  }

  val selfClosingActor = system.actorOf(Props[SelfClosingActor], "selfClosingActor")
  //Should close after 1 ping sent
  /*
  system.scheduler.scheduleOnce(250 millis){
    selfClosingActor ! "ping"
  } */

  //self closing actor should never close
  val routine3 =
    system.scheduler.scheduleWithFixedDelay(Duration.Zero, 50.milliseconds, selfClosingActor,"ping")

  //unless we decice to kill
  system.scheduler.scheduleOnce(5 seconds){
    selfClosingActor ! "timeout"
  }

  //Timers stuff
  //Basically and actor that sends messages to itself based on periodic behaviour using Timers
  case object TimerKey
  case object Start
  case object Reminder
  case object Stop
  class TimerBasedHeartbeatActor extends Actor with ActorLogging with Timers {
    timers.startSingleTimer(TimerKey, Start, 10 millis)

    override def receive: Receive = {
      case Start =>
        log.info("Bootstrapping")
        timers.startTimerWithFixedDelay(TimerKey,Reminder, 10 millis)
      case Reminder =>
        log.info("I am alive")
      case Stop =>
        log.warning("Stopping!")
        timers.cancel(TimerKey)
        context.stop(self)
    }
  }

  val timerBasedHeartbeatActor = system.actorOf(Props[TimerBasedHeartbeatActor], "timerActor")
  system.scheduler.scheduleOnce( 10 seconds) {
    timerBasedHeartbeatActor ! Stop
  }





}
