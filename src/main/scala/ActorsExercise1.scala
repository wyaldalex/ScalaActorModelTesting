import akka.actor.{Actor, ActorRef, ActorSystem, Props}

object ActorsExercise1 extends App {

  //Exercise 1 incremement decrement counter
  val actorSystem = ActorSystem("xActorSystem")

  //Even a more simple actor thar reduces a list
  class CounterActor extends Actor {

    var counter = 0

    override def receive: Receive = {
      case message : "Increment" => {
        counter += 1
        println(s"New value: $counter")
      }
      case message : "Decrement" => {
        counter -= 1
        println(s"New value: $counter")
      }
      case _ =>
    }
  }

  val counterActor = actorSystem.actorOf(Props[CounterActor], "counterActor")
  counterActor ! "Increment"
  counterActor ! "Increment"
  counterActor ! "Increment"
  counterActor ! "Decrement"
  counterActor ! "Increment"
  counterActor ! "Increment"

  //Exercise 2
  case class BankTransaction(id : Int, transType : String, amount : Double, ref: ActorRef)

  class BankAccount extends Actor {

    var funds : Double = 0

    override def receive: Receive = {
      case  bankTransaction : BankTransaction => {
        try {

          if(bankTransaction.transType.equals("Deposit")){
            funds += bankTransaction.amount
            println(s"Remaining funds $funds")
            bankTransaction.ref ! "Success"
          } else {
            funds -= bankTransaction.amount
            println(s"Remaining funds $funds")
            bankTransaction.ref ! "Success"
          }

        } catch {
          case e : Exception => bankTransaction.ref ! "Failure"
        }

      }
      case "Success" => println("Last transaction SUCCESFUL")
      case "Failure" => println("Last transaction FAILED")
      case _ =>
    }
  }

  val bankActor = actorSystem.actorOf(Props[BankAccount], "bankAccount1")
  val bankActor2 = actorSystem.actorOf(Props[BankAccount], "bankAccount2")

  bankActor ! BankTransaction(12121,"Deposit", 800.2, bankActor2)
  bankActor ! BankTransaction(12121,"Deposit", 800.4, bankActor2)
  bankActor ! BankTransaction(12121,"Withdraw", 400.3, bankActor2)


}
