package task.modules.fileReader

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class FileReaderTest extends AnyFlatSpec with Matchers {

  "videoIdsFromFile with unexistent file" should "throw FileNotFound exception" in {
    an[java.io.FileNotFoundException] should be thrownBy FileReader
      .videoIdsFromFile("dewfwefwe")
  }

  "wrong data in file" should "return empty set" in {
      assert(FileReader.videoIdsFromFile("src/test/scala/task/modules/fileReader/test.txt") == Set())
  }

  "empty file" should "return empty set" in {
      assert(FileReader.videoIdsFromFile("src/test/scala/task/modules/fileReader/emptyTest.txt") == Set())
  }

}
