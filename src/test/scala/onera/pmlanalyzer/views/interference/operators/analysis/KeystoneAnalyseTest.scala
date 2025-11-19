package onera.pmlanalyzer.views.interference.operators.analysis

import onera.pmlanalyzer.pml.model.instances.keystone.{
  KeystonePlatform,
  KeystoneRoutingConstraints,
  RosaceConfiguration
}
import onera.pmlanalyzer.pml.model.utils.Message
import onera.pmlanalyzer.views.interference.InterferenceTestExtension.*
import onera.pmlanalyzer.views.interference.model.formalisation.InterferenceCalculusProblem.Method
import onera.pmlanalyzer.views.interference.model.formalisation.SolverImplm
import onera.pmlanalyzer.views.interference.model.formalisation.SolverImplm.*
import onera.pmlanalyzer.views.interference.model.specification.keystone.RosaceInterferenceSpecification
import onera.pmlanalyzer.views.interference.operators.*
import org.chocosolver.solver.exception.InvalidSolutionException
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps
import scala.util.{Failure, Success, Try}

class KeystoneAnalyseTest extends AnyFlatSpec with should.Matchers {

  private val expectedResultsDirectoryPath = "keystone"

  object KeystoneWithRosace
      extends KeystonePlatform
      with RosaceConfiguration
      with KeystoneRoutingConstraints
      with RosaceInterferenceSpecification

  KeystoneWithRosace.fullName should "contain the expected semantics distribution" taggedAs FastTests in {
    val semanticsDistribution =
      KeystoneWithRosace.getSemanticsSize(ignoreExistingFile = true)
    semanticsDistribution(2) should be(1194)
    semanticsDistribution(3) should be(15984)
    semanticsDistribution(4) should be(138417)
    semanticsDistribution(5) should be(811356)
    semanticsDistribution(6) should be(3263780)
    semanticsDistribution(7) should be(8904128)
    semanticsDistribution(8) should be(15777408)
    semanticsDistribution(9) should be(16404480)
    semanticsDistribution(10) should be(7603200)
    semanticsDistribution(11) should be(0)
    semanticsDistribution(12) should be(0)
    semanticsDistribution(13) should be(0)
  }

  val kForFastTest = 4

  private def compareWithExpected(
      k: Int,
      implm: SolverImplm,
      method: Method
  ): Unit = {
    if (implm == Monosat) {
      assume(
        monosatLibraryLoaded,
        Message.monosatLibraryNotLoaded
      )
    }
    Try({
      Await.result(
        KeystoneWithRosace
          .test(k, expectedResultsDirectoryPath, implm, method),
        1 hour
      )
    }) match {
      case Failure(exception: InvalidSolutionException) =>
        assume(false, exception.getMessage)
      case Failure(exception) =>
        fail(exception.getMessage)
      case Success(diff) =>
        if (diff.exists(_.nonEmpty)) {
          fail()
        }
    }
  }

  for {
    method <- Method.values
    implm <- SolverImplm.values
  } {
    s"For ${KeystoneWithRosace.fullName} limited to $kForFastTest-multi-transactions, the $method method implemented with $implm" should "find the verified interference" taggedAs FastTests in {
      compareWithExpected(kForFastTest, implm, method)
    }
  }

  for {
    method <- Method.values
    implm <- SolverImplm.values
  } {
    s"For ${KeystoneWithRosace.fullName}, the $method method implemented with $implm" should "find the verified interference" taggedAs PerfTests in {
      compareWithExpected(13, implm, method)
    }
  }
}
