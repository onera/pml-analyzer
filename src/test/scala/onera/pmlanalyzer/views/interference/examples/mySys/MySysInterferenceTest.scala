package onera.pmlanalyzer.views.interference.examples.mySys

import onera.pmlanalyzer.pml.examples.mySys.MySysExport.MySys
import onera.pmlanalyzer.views.interference.InterferenceTestExtension
import onera.pmlanalyzer.views.interference.operators.*
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should
import onera.pmlanalyzer.views.interference.InterferenceTestExtension.*

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

class MySysInterferenceTest extends AnyFlatSpec with should.Matchers {

  private val expectedResultsDirectoryPath = "mySys"

  "For MySys, the interference analysis" should "find the verified interference" in {
    val diff =
      Await.result(MySys.test(4, expectedResultsDirectoryPath), 10 minutes)
    if (diff.exists(_.nonEmpty)) {
      fail(diff.map(InterferenceTestExtension.failureMessage).mkString("\n"))
    }
  }
}
