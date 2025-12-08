package onera.pmlanalyzer.views.interference.operators.analysis

import onera.pmlanalyzer.pml.model.instances.keystone.KeystoneWithRosace
import onera.pmlanalyzer.pml.model.utils.Message
import onera.pmlanalyzer.views.interference.InterferenceTestExtension.*
import onera.pmlanalyzer.views.interference.model.formalisation.InterferenceCalculusProblem.Method
import onera.pmlanalyzer.views.interference.model.formalisation.SolverImplm
import onera.pmlanalyzer.views.interference.model.formalisation.SolverImplm.*
import org.chocosolver.solver.exception.InvalidSolutionException
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should
import onera.pmlanalyzer.*
import onera.pmlanalyzer.views.interference.operators.Analyse

import scala.concurrent.{Await, TimeoutException}
import scala.concurrent.duration.{DurationInt, FiniteDuration}
import scala.language.postfixOps
import scala.util.{Failure, Success, Try}

class KeystoneAnalyseTest extends AnyFlatSpec with should.Matchers {

  private val expectedResultsDirectoryPath = "keystone"

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

  private val kForFastTest = 4
  private val TIS =
    KeystoneWithRosace.computeTopologicalInterferenceSystem(kForFastTest)

  private def compareWithExpected[T: Analyse](
      x: T,
      k: Int,
      implm: SolverImplm,
      method: Method,
      timeout: FiniteDuration
  ): Unit = {
    for { m <- implm.checkDependencies() } yield {
      cancel(m)
    }

    Try({
      Await.result(
        x.test(k, expectedResultsDirectoryPath, implm, method),
        timeout
      )
    }) match {
      case Failure(exception: InvalidSolutionException) =>
        exception.printStackTrace()
        cancel()
      case Failure(exception: TimeoutException) =>
        cancel("[WARNING] timeout during interference computation")
      case Failure(exception) =>
        exception.printStackTrace()
        fail()
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
    s"For ${KeystoneWithRosace.fullName}, the analysis operator limited to $kForFastTest-multi-transactions, the $method method implemented with $implm" should "find the verified interference" taggedAs FastTests in {
      compareWithExpected(
        KeystoneWithRosace,
        kForFastTest,
        implm,
        method,
        2 minutes
      )
    }
  }

  for {
    method <- Method.values
    implm <- SolverImplm.values
  } {

    s"For ${KeystoneWithRosace.fullName}, the analysis operator limited to $kForFastTest-multi-transactions with the $method method implemented, with $implm, with topological interference system export" should "find the verified interference" taggedAs PerfTests in {
      compareWithExpected(TIS, kForFastTest, implm, method, 10 minutes)
    }
  }

  for {
    method <- Method.values
    implm <- SolverImplm.values
  } {
    s"For ${KeystoneWithRosace.fullName}, the analysis operator with the $method method implemented with $implm" should "find the verified interference" taggedAs PerfTests in {
      compareWithExpected(KeystoneWithRosace, 13, implm, method, 1 hour)
    }
  }
}
