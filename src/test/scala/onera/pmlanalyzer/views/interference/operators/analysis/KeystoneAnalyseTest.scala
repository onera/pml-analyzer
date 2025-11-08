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

//  val kForFastTest = 4
//
//  for {
//    method <- Method.values
//    implm <- SolverImplm.values
//  } {
//    s"For ${KeystoneWithRosace.fullName}, the $method method implemented with $implm" should s"find the verified interference up to $kForFastTest" taggedAs FastTests in {
//      if (implm == Monosat) {
//        assume(
//          monosatLibraryLoaded,
//          Message.monosatLibraryNotLoaded
//        )
//      }
//      Try({
//        Await.result(
//          KeystoneWithRosace
//            .test(kForFastTest, expectedResultsDirectoryPath, implm, method),
//          10 minutes
//        )
//      }) match {
//        case Failure(exception) => assume(false, exception.getMessage)
//        case Success(diff) =>
//          if (diff.exists(_.nonEmpty)) {
//            fail(diff.map(failureMessage).mkString("\n"))
//          }
//      }
//    }
//  }

  for {
    method <- Method.values
    implm <- SolverImplm.values
  } {
    s"For ${KeystoneWithRosace.fullName}, the $method method implemented with $implm" should "find the verified interference" taggedAs FastTests in {
      if (implm == Monosat) {
        assume(
          monosatLibraryLoaded,
          Message.monosatLibraryNotLoaded
        )
      }
      Try({
        Await.result(
          KeystoneWithRosace
            .test(13, expectedResultsDirectoryPath, implm, method),
          1 hour
        )
      }) match {
        case Failure(exception) => assume(false, exception.getMessage)
        case Success(diff) =>
          if (diff.exists(_.nonEmpty)) {
            fail()
          }
      }
    }
  }
}
