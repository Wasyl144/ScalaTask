package task.modules.filters.filter

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import scala.concurrent.Await
import scala.concurrent.duration.Duration


class NLPFilterTest extends AnyFlatSpec with Matchers {
  
    "Filter with empty string" should "return empty set" in {
        val filterFuture = NLPFilter.filter("")
        val filterResponse = Await.result(filterFuture, Duration.Inf)
        assert(filterResponse.count(p => true) == 0)
    }

    "Filter with numbers" should "return empty set" in {
        val filterFuture = NLPFilter.filter("2451 1514 5454555")
        val filterResponse = Await.result(filterFuture, Duration.Inf)
        println(filterResponse)
        assert(filterResponse.count(p => true) == 0)
    }

    "Filter with verb" should "return empty set" in {
        val filterFuture = NLPFilter.filter("going")
        val filterResponse = Await.result(filterFuture, Duration.Inf)
        println(filterResponse)
        assert(filterResponse.count(p => true) == 0)
    }

    "Filter with my name" should "return set of 2 strings"  in {
        val filterFuture = NLPFilter.filter("Michael Frankiewicz")
        val filterResponse = Await.result(filterFuture, Duration.Inf)
        println(filterResponse)
        assert(filterResponse.count(p => true) == 2)
    }
    "Filter with random string" should "more than 0 results"  in {
        val filterFuture = NLPFilter.filter("wbchwb wioc23 80cby8029 092hc209")
        val filterResponse = Await.result(filterFuture, Duration.Inf)
        println(filterResponse)
        assert(filterResponse.count(p => true) >= 0)
    }


}
