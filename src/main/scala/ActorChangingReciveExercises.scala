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

  /*
  * Exercise 2 - a simplified voting system
   */
  case class Vote(candidate: String)
  case object VoteStatusRequest
  case class VoteStatusReply(candidate: Option[String])

  class Citizen extends Actor {
    var choice: String = ""
    override def receive: Receive = noVoteYet

    def alreadyVoted: Receive = {
      case VoteStatusRequest => {
        println(s"Citizen ${this.self} already voted for ${Some(choice).get}")
        sender() ! VoteStatusReply(Some(choice))
      }
    }

    def noVoteYet: Receive = {
      case Vote(candidate) => {
        choice = candidate
        context.become(alreadyVoted)
      }
      case VoteStatusRequest => {
        println(s"Citizen ${this.self} has not voted yet")
        0
      }
    }
  }

  case class AggregateVotes(citizen: Set[ActorRef])
  case object PrintResults
  class VoteAggregator extends Actor {

    var voteResults = new Array[String](1)
    var counter = 0

    override def receive: Receive = {
      case AggregateVotes(voters) => {
        /*I need a data structure like a map, in which I can have the candidate name
          and the votes for that candidate, if the map already contains the option
          then just increase the number else add a new entry on the result map
         */
        voteResults = new Array[String](voters.size)
        voters.foreach(voter => {
          println("Polling voter")
          voter ! VoteStatusRequest
        })
        this.self ! PrintResults
      }
      case VoteStatusReply(candidate) => {
        println(s"Processing vote $counter for ${candidate.get}")
        voteResults(counter) = candidate.get
        counter += 1
      }
      case PrintResults => {

        if(voteResults.length != 0 && voteResults.last != null){
          voteResults.foreach(x => println(x))
          var setResults = voteResults.toSet
          var votesForCandidate = 0
          setResults.foreach(candidateName => {
            voteResults.foreach(candidateNameVote => {
              if(candidateName == candidateNameVote){
                votesForCandidate += 1
              }
            })
            println(s"$candidateName -> $votesForCandidate")
            votesForCandidate = 0
          })

        } else {
          this.self ! PrintResults
        }

      }
    }
  }

  val alice = actorSystem.actorOf(Props[Citizen], "alice")
  val oscar = actorSystem.actorOf(Props[Citizen], "oscar")
  val sasha = actorSystem.actorOf(Props[Citizen], "sasha")
  val bob = actorSystem.actorOf(Props[Citizen], "bob")
  val michael = actorSystem.actorOf(Props[Citizen], "michael")
  val katherine = actorSystem.actorOf(Props[Citizen], "katherine")

  //alice ! VoteStatusRequest
  alice ! Vote("Napoleon")
  bob ! Vote("Napoleon")
  michael ! Vote("Cristian")
  katherine ! Vote("Martin")
  sasha ! Vote("Napoleon")
  oscar ! Vote("Martin")

//  alice ! VoteStatusRequest



  /*
  Expected Result:
  Napoleon -> 2
  Jonas -> 1
  Roland -> 1
   */
  val voteAggregator = actorSystem.actorOf(Props[VoteAggregator], "voteAggregator")
  voteAggregator ! AggregateVotes(Set(alice, bob,michael,katherine,sasha,oscar))
  //voteAggregator ! PrintResults







}
