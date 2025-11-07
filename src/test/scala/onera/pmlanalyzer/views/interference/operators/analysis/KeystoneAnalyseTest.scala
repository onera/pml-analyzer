package onera.pmlanalyzer.views.interference.operators.analysis

import onera.pmlanalyzer.pml.model.configuration.TransactionLibrary.UserTransactionId
import onera.pmlanalyzer.views.interference.operators.*
import onera.pmlanalyzer.views.interference.exporters.*
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should
import onera.pmlanalyzer.pml.model.instances.keystone.{KeystonePlatform, KeystoneRoutingConstraints, RosaceConfiguration}
import onera.pmlanalyzer.pml.model.utils.Message
import onera.pmlanalyzer.views.interference.model.specification.keystone.RosaceInterferenceSpecification

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps
import onera.pmlanalyzer.views.interference.InterferenceTestExtension.*
import onera.pmlanalyzer.views.interference.{Missing, Unknown}
import onera.pmlanalyzer.views.interference.model.formalisation.InterferenceCalculusProblem.Method
import onera.pmlanalyzer.views.interference.model.formalisation.InterferenceCalculusProblem.Method.{Default, GroupedLitBased}
import onera.pmlanalyzer.views.interference.model.formalisation.SolverImplm
import onera.pmlanalyzer.views.interference.model.formalisation.SolverImplm.*
import onera.pmlanalyzer.views.interference.model.specification.InterferenceSpecification.PhysicalTransactionId

import scala.util.{Failure, Success, Try}

class KeystoneAnalyseTest extends AnyFlatSpec with should.Matchers {

  private val expectedResultsDirectoryPath = "keystone"

  object KeystoneWithRosace
      extends KeystonePlatform
      with RosaceConfiguration
      with KeystoneRoutingConstraints
      with RosaceInterferenceSpecification

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
            .test(4, expectedResultsDirectoryPath, implm, method),
          10 minutes
        )
      }) match {
        case Failure(exception) => assume(false, exception.getMessage)
        case Success(diff)      =>
          for {
            (dS,i) <- diff.zipWithIndex
            if dS.nonEmpty && i <=1
            d <- dS
          } {
            d match {
              case Missing(_, isFree) => 
                KeystoneWithRosace.exportInterferenceGraphFromString(d.s.toSet,Some(s"missing_${if(isFree) "free" else "itf"}"))
              case Unknown(_,isFree) => 
                KeystoneWithRosace.exportInterferenceGraphFromString(d.s.toSet,Some(s"unknown_${if(isFree) "free" else "itf"}"))
            }
          }
          if (diff.exists(_.nonEmpty)) {
            fail(diff.map(failureMessage).mkString("\n"))
          }
      }
    }
  }
}
