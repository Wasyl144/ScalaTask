package task

final case class YTReady(id: String, source: String, plainText: String) extends Identifable with FromSourceable with PlainTexted