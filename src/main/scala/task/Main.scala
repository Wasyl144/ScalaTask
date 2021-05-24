package task

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory

object Main extends App{
  // Init akka.actors
  implicit val system = ActorSystem("Task")

  // Init Akka streams
  implicit val materializer = ActorMaterializer()

  // load config 
  val config = ConfigFactory.load()

  try {
    args match {
      case arr if arr.length != 1 =>
        println(f"Usage: run [input file]")
        ()
      case arr =>{
        val cfg = MyConfig(config.getString("app.LANG"))
        Pipeline.boot(cfg, arr.head)
      }
    }
  } catch {
    case e: Exception => println(e)
  }
}
