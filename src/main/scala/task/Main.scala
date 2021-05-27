package task

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory

object Main extends App{
  val logger = LoggerFactory.getLogger(getClass().getSimpleName())

  // Init akka.actors
  implicit val system = ActorSystem("Task")

  // Init Akka streams
  implicit val materializer = ActorMaterializer()

  // load config 
  val config = ConfigFactory.load()

  try {
    args match {
      case arr if arr.length != 1 =>
        logger error(f"Usage: run [input file]")
        ()
      case arr =>{
        val cfg = MyConfig(config.getString("app.LANG"))
        Pipeline.boot(cfg, arr.head)
      }
    }
  } catch {
    case e: Exception => logger error (e.getMessage())
  }
}
