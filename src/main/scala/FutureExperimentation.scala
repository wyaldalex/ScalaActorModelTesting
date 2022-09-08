import akka.actor.{Actor, ActorRef, ActorSystem, Props}

import scala.concurrent._
import scala.concurrent.duration._
import scala.util.{Failure, Random, Success}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.postfixOps

object FutureExperimentation extends App {

  case class Event(id: Int, info: String, location: Int, time: Int)
  case class EventRequest(tickerNr: Int)
  case class EventResponse(headers: String, event: Event)
  case class TrafficRequest(location: Int, time: Int)
  case class TrafficResponse(route: String)
  //mock web service request
  def callEventService(request: EventRequest): EventResponse = {
    println("Obtaining the Event Service Response")
    Thread.sleep(4000)
    EventResponse("dummy headers", Event(Random.between(1, 8000),"Some dummy info",Random.between(1, 8000), Random.between(1, 8000)))
  }

  def callTrafficService(request: TrafficRequest): TrafficResponse = {
    println("Obtaining the Traffic Service Response")
    Thread.sleep(4000)
    TrafficResponse("some route xyxyxyx 178 degrees , lat 12121")
  }

  // Synchronous approach
  /*
  val request = EventRequest(121)
  val eventResponse = callEventService(request)
  println(eventResponse.event.id) */

  //Asynchronous approach

  val request2 = EventRequest(122)
  val futureEvent: Future[Event] = Future {
    val response = callEventService(request2)
    response.event
  }

  //Chaining the response
  futureEvent.foreach{ event =>
    val trafficRequest = TrafficRequest(event.location, event.time)
    val trafficResponse = callTrafficService(trafficRequest)
    println(trafficResponse.route)
  }

  val futureRoute:Future[String] = futureEvent.map{ event =>
    val trafficRequest = TrafficRequest(event.location, event.time)
    val trafficResponse = callTrafficService(trafficRequest)
    trafficResponse.route
  }
  //Then you can obtain the result of the futureRoute with some Await utility

  //Further optimization and refactoring
  def getEvent(ticketId: Int): Future[Event] = {
    println(s"Obtaining the Event Service Response using id $ticketId")
    Thread.sleep(8000)
    Future(Event(Random.between(1, 8000),"Some dummy info",Random.between(1, 8000), Random.between(1, 8000)))
  }

  def getRoute(event: Event): Future[String] = {
    println(s"Obtaining the Traffic Service Response using event id ${event.id}")
    Thread.sleep(4000)
    Future("some route xyxyxyx 178 degrees , lat 12121 From the refactor method using Event ")
  }

  val futurRoute2 = getEvent(1212).map{ event =>
    getRoute(event)
  }
  futurRoute2.onComplete({
    case Success(route) => {
      println(s"Result of the future: $route")

    }
    case Failure(ex) => {
      println("Some exception")
    }
  })

  Thread.sleep(22000)

}
