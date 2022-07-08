import akka.actor.{Actor, ActorSystem, Props}

object ActorsIntro extends App {


  //part 1 - actor systems
  val actorSystem = ActorSystem("firstActorSystem")
  println(actorSystem.name)

  // part 2 - create actors
  //actor 1
  class WordCountActor extends Actor {
    //internal data
    var totalWords = 0

    //behaviour
    def receive: PartialFunction[Any, Unit] = {
      case message: String => println(s"[word counter received message]: $message")
         //println(s"[Message contains this amount of words:]: $totalWords")
         //totalWords += message.split(" ").length
      case msg => println(s"[word counter] I cannot understand ${msg.toString}")
    }
  }
    //part 3 - instantiate our actor
    val wordCounter = actorSystem.actorOf(Props[WordCountActor], "wordCounter")
    val anotherWordCounter = actorSystem.actorOf(Props[WordCountActor], "anotherWordCounter")

    //part 4  - communicate!
    wordCounter ! "I am learning Akka and its nice"
    anotherWordCounter ! "Some other message"
   //asynchronous call
  // "!" Is read as "Tells" Like "Person Tells Something"

   class Person(name: String) extends Actor {
     override def receive: Receive = {
       case "Bob" => println(s"Hi my name is $name")
       case _ =>

     }
   }

  val person = actorSystem.actorOf(Props(new Person("Bob Simpson")))
  person ! "Bob"

  //Or companion object pattern
  object Person {
    def props(name: String) = Props(new Person(name))
  }
  val person2 = actorSystem.actorOf(Person.props("Bob Sidious"))
  person2 ! "Bob"








}
