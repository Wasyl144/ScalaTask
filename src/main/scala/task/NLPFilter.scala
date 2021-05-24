package task

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import edu.stanford.nlp.coref.data.CorefChain
import edu.stanford.nlp.ling._
import edu.stanford.nlp.ie.util._
import edu.stanford.nlp.pipeline._
import edu.stanford.nlp.semgraph._
import edu.stanford.nlp.trees._
import java.{util => ju}
import collection.JavaConverters._

object NLPFilter extends NounFilter {
  def filter(text: String): Future[Set[String]] = Future {
    println("before \n")
    // println(text)

    val props: ju.Properties = new ju.Properties
    props.setProperty("annotators", "tokenize,ssplit,pos,parse")
    props.setProperty("coref.algorithm", "neural")

    val pipeline: StanfordCoreNLP = new StanfordCoreNLP(props)

    val document: CoreDocument = new CoreDocument(text)

    println("document")

    pipeline.annotate(document)
    println("pipe")

    val nouns: Set[String] = document
      .sentences()
      .asScala
      .flatMap(text => {
        text
          .tokens()
          .asScala
          .filter(word => {
            word.tag().contains("NN") && text.posTags() != null
          })
      })
      .map(filteredNoun => filteredNoun.originalText()).toSet
    nouns
  }
}