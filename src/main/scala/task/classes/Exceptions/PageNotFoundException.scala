package task.classes.Exceptions

final case class PageNotFoundException(private val message: String = "Page not Found", private val cause: Throwable = None.orNull) extends Exception(message, cause)
