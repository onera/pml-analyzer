package onera.pmlanalyzer.views.interference.operators.analysis

import onera.pmlanalyzer.views.interference.operators.*
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should
import onera.pmlanalyzer.pml.model.instances.keystone.{KeystonePlatform, KeystoneRoutingConstraints, RosaceConfiguration}
import onera.pmlanalyzer.pml.model.utils.Message
import onera.pmlanalyzer.views.interference.model.specification.keystone.RosaceInterferenceSpecification

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps
import onera.pmlanalyzer.views.interference.InterferenceTestExtension.*
import onera.pmlanalyzer.views.interference.model.formalisation.SolverImplm.*

class KeystoneAnalyseTest extends AnyFlatSpec with should.Matchers {

  private val expectedResultsDirectoryPath = "keystone"

  object KeystoneWithRosace extends KeystonePlatform
    with RosaceConfiguration
    with KeystoneRoutingConstraints
    with RosaceInterferenceSpecification

  "For Keystone, the staged monosat interference solver" should "find the verified interference" taggedAs FastTests  in {
    assume(
      monosatLibraryLoaded,
      Message.monosatLibraryNotLoaded
    )
    val diff = Await.result(KeystoneWithRosace.test(4, expectedResultsDirectoryPath, Monosat), 10 minutes)
    if (diff.exists(_.nonEmpty)) {
      fail(diff.map(failureMessage).mkString("\n"))
    }
  }

  "For Keystone, the staged Choco interference solver" should "find the verified interference" taggedAs FastTests  in {
    val diff = Await.result(KeystoneWithRosace.test(4, expectedResultsDirectoryPath, Choco), 10 minutes)
    if (diff.exists(_.nonEmpty)) {
      fail(diff.map(failureMessage).mkString("\n"))
    }
  }
}