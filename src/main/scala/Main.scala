import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import java.nio.file.Paths
object Main extends App {
  
  /*
  * Initialize values to use in prgram
  *
  */
  
  val PATH : String = System.getProperty("user.dir")
  val FILENAME : String = "/testFile.txt"
  val PATHTOFILE : String = PATH + FILENAME 
  val API_KEY : String = "AIzaSyDgDRf94mv632cyVhg0EoTVQ-5YCmb4lPQ"



  println(API_KEY)


  
}