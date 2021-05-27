package task

import scala.concurrent.Future

trait NounFilter {
  def filter(text: String): Future[Set[String]]
}
