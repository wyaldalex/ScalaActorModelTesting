import ActorChangingReciveExercises.callerSystemActor.Start
import akka.actor.{Actor, ActorRef, ActorSystem, Props}

object ActorOffspringExercises extends App {

  val actorSystem = ActorSystem("xActorSystem")

  object WordCounterMaster {
    case class Initialize(nChildren: Int)
    case class WordCountTask(childId: Int, text: String)
    case class WordCountReply(text: String, count: Int)
  }

  class WordCounterMaster extends Actor {

    import WordCounterMaster._
    var totalWorkers : Int = 0

    override def receive: Receive = {
      case Initialize(nChildren) => {
        //Create child actors of type WordCounterWorker
        println(s"${self.path} creating offspring")
        for (i <- 1 to nChildren) {
          val workerRef = context.actorOf(Props[WordCounterWorker], s"worker$i")
        }
        totalWorkers = nChildren
        context.become(withWorkersSetup(1))
      }
    }

    def withWorkersSetup(currentWorker: Int): Receive = {
      case WordCountTask(workerNumber, text) => {
        //Pick a worker In Round Robin Fashion and process the text
        println(s"Text Received for processing, allocating worker number $currentWorker")
        val workerSelection = actorSystem.actorSelection(s"/user/wcm/worker$currentWorker")
        workerSelection forward text
        context.become(withWorkersSetup(if(currentWorker >= totalWorkers) {1} else  {currentWorker + 1}))

      }
      case WordCountReply(childN, count) => {
        println(s"Master Word Counter ${self.path} received count $count")
      }
    }
  }

  class WordCounterWorker extends Actor {

    import WordCounterMaster._

    override def receive: Receive = {
      case text : String => {
        println(s"Worker ${self.path} Receving text to process: $text")
        sender() ! WordCountReply(text,text.split(" ").length)
      }
    }
  }

  object WordCounterRequester {
    case class InitializeMaster(nWorkers : Int, wcm: ActorRef)
    case class CountWords(text: String,wcm: ActorRef)
  }
  class WordCounterRequester extends Actor {
    import WordCounterRequester._
    import WordCounterMaster._

    override def receive: Receive = {
      case InitializeMaster(workersNumber, actorRef) => {
        actorRef ! Initialize(workersNumber)
      }
      case CountWords(text, actorRef) => {
        actorRef ! WordCountTask(1,text)

      }
      case WordCountReply(text, count) => {
        println(s"The Text \"$text\" has $count words")
      }
    }
  }

  import WordCounterMaster._
  import WordCounterRequester._
  val wordCounterRequester = actorSystem.actorOf(Props[WordCounterRequester],"wcr")
  val wordCounterMaster = actorSystem.actorOf(Props[WordCounterMaster],"wcm")
  wordCounterRequester ! InitializeMaster(10,wordCounterMaster)
  wordCounterRequester ! CountWords("Akka is new to me but, is very nice", wordCounterMaster)
  wordCounterRequester ! CountWords("Akka is new to me but, is very nice also", wordCounterMaster)
  wordCounterRequester ! CountWords("Akka is new to me but, is very nice also again", wordCounterMaster)
  wordCounterRequester ! CountWords("Alice In Wonderland", wordCounterMaster)
  wordCounterRequester ! CountWords("Star Wars", wordCounterMaster)
  wordCounterRequester ! CountWords("Wolf of WallStreet", wordCounterMaster)
  wordCounterRequester ! CountWords("The Lord of The Rings, The Twin Towers", wordCounterMaster)
  wordCounterRequester ! CountWords("Breaking Bad", wordCounterMaster)
  wordCounterRequester ! CountWords("Better Call Saul", wordCounterMaster)
  wordCounterRequester ! CountWords("Full Metal Jacket", wordCounterMaster)
  wordCounterRequester ! CountWords("Star Wars Episode III, The Revenge of the Sith", wordCounterMaster)
  wordCounterRequester ! CountWords("Scarface", wordCounterMaster)
  wordCounterRequester ! CountWords("Locomotor Promotor Accumulator Doctor Sculptor Skeletor", wordCounterMaster)
  wordCounterRequester ! CountWords("The 9th Gate", wordCounterMaster)

  //wordCounterMaster ! Initialize(10)
  //wordCounterMaster ! WordCountTask(1,"Akka is new to me but, is very nice")
  //wordCounterMaster ! WordCountTask(7,"Akka is new to me but, is very nice")






  /*
  send Initialize(10) to wordCounterMaster
  send "Akka is awesome" to wordCounterMaster
    wcm will send WordCountTask(..) to one of its children
     child replies with a WordCountReply(3) to Master
     master replies with 3 to the sender

     requester -> wcm -> wcw
                      r <- wcm <-
      //round robin logic

   */


}
