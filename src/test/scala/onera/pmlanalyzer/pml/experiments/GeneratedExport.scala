package onera.pmlanalyzer.pml.experiments

import onera.pmlanalyzer.GnuPlotWriter
import onera.pmlanalyzer.pml.exporters.*
import onera.pmlanalyzer.pml.model.utils.Message
import onera.pmlanalyzer.pml.operators.*
import onera.pmlanalyzer.views.interference.exporters.*
import onera.pmlanalyzer.views.interference.model.specification.{InterferenceSpecification, TableBasedInterferenceSpecification}
import onera.pmlanalyzer.views.interference.operators.*
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should

import scala.concurrent.duration.*
import scala.language.postfixOps
import onera.pmlanalyzer.views.interference.InterferenceTestExtension.*

class GeneratedExport extends AnyFlatSpec with should.Matchers {

  object GeneratedFull extends GeneratedPlatform
    with GeneratedSoftwareAllocation
    with GeneratedTransactionLibrary
    with TableBasedInterferenceSpecification{
  }

  "GeneratedFull" should "be exportable" in {
    GeneratedFull.exportRestrictedHWAndSWGraph()
    GeneratedFull.exportDataAllocationTable()
    GeneratedFull.exportUserTransactions()
    GeneratedFull.exportPhysicalTransactions()
    GeneratedFull.exportUserScenarios()
    GeneratedFull.exportSemanticsSize()
  }

  it should "be possible to compute the interference" in {
    GeneratedFull.computeAllInterference(1 minutes, ignoreExistingAnalysisFiles = true)
    println(s"Semantic reduction is: ${GeneratedFull.computeSemanticReduction()}")
    println(s"Graph reduction is: ${GeneratedFull.computeGraphReduction()}")
  }


}
