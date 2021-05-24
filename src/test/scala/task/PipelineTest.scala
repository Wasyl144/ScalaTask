package task

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.language.postfixOps


class PipelineTest extends AnyFlatSpec with Matchers {
    
    
    "videoIdsFromFile with unexistent file" should "throw FileNotFound exception" in {
        an[java.io.FileNotFoundException] should be thrownBy Pipeline.videoIdsFromFile("dewfwefwe")
    }

    

}