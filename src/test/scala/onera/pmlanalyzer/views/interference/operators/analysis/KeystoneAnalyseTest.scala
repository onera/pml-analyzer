package onera.pmlanalyzer.views.interference.operators.analysis

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
import onera.pmlanalyzer.views.interference.model.formalisation.InterferenceCalculusProblem.Method
import onera.pmlanalyzer.views.interference.model.formalisation.InterferenceCalculusProblem.Method.{Default, GroupedLitBased}
import onera.pmlanalyzer.views.interference.model.formalisation.SolverImplm
import onera.pmlanalyzer.views.interference.model.formalisation.SolverImplm.*

class KeystoneAnalyseTest extends AnyFlatSpec with should.Matchers {

  private val expectedResultsDirectoryPath = "keystone"

  object KeystoneWithRosace
      extends KeystonePlatform
      with RosaceConfiguration
      with KeystoneRoutingConstraints
      with RosaceInterferenceSpecification

  for {
    method <- Seq(Default)//Method.values
    implm <- Seq(Choco)//SolverImplm.values
  }{
    s"For ${KeystoneWithRosace.fullName}, the $method method implemented with $implm" should "find the verified interference" taggedAs FastTests in {
      if(implm == Monosat) {
        assume(
          monosatLibraryLoaded,
          Message.monosatLibraryNotLoaded
        )
      }
      val toExport = Set(
        "KeystoneWithRosace_EDMA_load_KeystoneWithRosace_SPI_load_0|KeystoneWithRosace_EDMA_store_KeystoneWithRosace_MSMC_SRAM_Bank0_store_0",
        "KeystoneWithRosace_ARMPac_ARM0_core_load_KeystoneWithRosace_DDR_Bank0_load_0",
        "KeystoneWithRosace_CorePac4_dsp_load_KeystoneWithRosace_CorePac2_dsram_load_0"
      )
      
      KeystoneWithRosace.exportInterferenceGraph(KeystoneWithRosace.purifiedTransactions.keySet.filter(
        k => toExport.contains(k.id.name)
      ))
      val diff = Await.result(
        KeystoneWithRosace.test(4, expectedResultsDirectoryPath, implm, method),
        10 minutes
      )
      if (diff.exists(_.nonEmpty)) {
        fail(diff.map(failureMessage).mkString("\n"))
      }
    }
  }
}
