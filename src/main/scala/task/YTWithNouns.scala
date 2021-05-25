package task

final case class YTWithNouns(id: String, source: String, plainText: String, list: Set[String]) extends Identifable with FromSourceable with PlainTexted with HasList[String]