/** *****************************************************************************
  * Copyright (c) 2023. ONERA This file is part of PML Analyzer
  *
  * PML Analyzer is free software ; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as published by the
  * Free Software Foundation ; either version 2 of the License, or (at your
  * option) any later version.
  *
  * PML Analyzer is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY ; without even the implied warranty of MERCHANTABILITY or
  * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
  * for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with this program ; if not, write to the Free Software Foundation,
  * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
  */

package onera.pmlanalyzer.pml.experiments

// import onera.pmlanalyzer.GnuPlotWriter
import onera.pmlanalyzer.pml.experiments.hbus.*

import onera.pmlanalyzer.pml.experiments.dbus.*

import onera.pmlanalyzer.pml.experiments.noc.*
import onera.pmlanalyzer.pml.exporters.*
import onera.pmlanalyzer.pml.model.configuration.TransactionLibrary
import onera.pmlanalyzer.pml.model.hardware.Platform
import onera.pmlanalyzer.pml.model.utils.Message
import onera.pmlanalyzer.pml.operators.*
import onera.pmlanalyzer.views.interference.InterferenceTestExtension.*
import onera.pmlanalyzer.views.interference.exporters.*
import onera.pmlanalyzer.views.interference.model.specification.{
  InterferenceSpecification,
  TableBasedInterferenceSpecification
}
import onera.pmlanalyzer.views.interference.operators.*
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should

import scala.concurrent.duration.*
import scala.language.postfixOps

class GeneratedPlatforms extends AnyFlatSpec with should.Matchers {

  object HbusCl2C2B8
      extends HbusCl2C2B8Platform
      with HbusCl2C2B8Software
      with HbusCl2C2B8TransactionLibrary
      with HbusCl2C2B8RoutingConstraints
      with TableBasedInterferenceSpecification {}

  object HbusCl2C4B8
      extends HbusCl2C4B8Platform
      with HbusCl2C4B8Software
      with HbusCl2C4B8TransactionLibrary
      with HbusCl2C4B8RoutingConstraints
      with TableBasedInterferenceSpecification {}

  object HbusCl4C2B8
      extends HbusCl4C2B8Platform
      with HbusCl4C2B8Software
      with HbusCl4C2B8TransactionLibrary
      with HbusCl4C2B8RoutingConstraints
      with TableBasedInterferenceSpecification {}

  object DbusC2D2B8
      extends DbusC2D2B8Platform
      with DbusC2D2B8Software
      with DbusC2D2B8TransactionLibrary
      with DbusC2D2B8RoutingConstraints
      with TableBasedInterferenceSpecification {}

  object DbusC2D4B8
      extends DbusC2D4B8Platform
      with DbusC2D4B8Software
      with DbusC2D4B8TransactionLibrary
      with DbusC2D4B8RoutingConstraints
      with TableBasedInterferenceSpecification {}

  object DbusC2D8B8
      extends DbusC2D8B8Platform
      with DbusC2D8B8Software
      with DbusC2D8B8TransactionLibrary
      with DbusC2D8B8RoutingConstraints
      with TableBasedInterferenceSpecification {}

  object DbusC4D2B8
      extends DbusC4D2B8Platform
      with DbusC4D2B8Software
      with DbusC4D2B8TransactionLibrary
      with DbusC4D2B8RoutingConstraints
      with TableBasedInterferenceSpecification {}

  object DbusC4D4B8
      extends DbusC4D4B8Platform
      with DbusC4D4B8Software
      with DbusC4D4B8TransactionLibrary
      with DbusC4D4B8RoutingConstraints
      with TableBasedInterferenceSpecification {}

  object DbusC4D8B8
      extends DbusC4D8B8Platform
      with DbusC4D8B8Software
      with DbusC4D8B8TransactionLibrary
      with DbusC4D8B8RoutingConstraints
      with TableBasedInterferenceSpecification {}

  object DbusC8D2B8
      extends DbusC8D2B8Platform
      with DbusC8D2B8Software
      with DbusC8D2B8TransactionLibrary
      with DbusC8D2B8RoutingConstraints
      with TableBasedInterferenceSpecification {}

  object DbusC8D4B8
      extends DbusC8D4B8Platform
      with DbusC8D4B8Software
      with DbusC8D4B8TransactionLibrary
      with DbusC8D4B8RoutingConstraints
      with TableBasedInterferenceSpecification {}

  object NocC4S2G1B8
      extends NocC4S2G1B8Platform
      with NocC4S2G1B8Software
      with NocC4S2G1B8TransactionLibrary
      with NocC4S2G1B8RoutingConstraints
      with TableBasedInterferenceSpecification {}

  object NocC4S2G2B8
      extends NocC4S2G2B8Platform
      with NocC4S2G2B8Software
      with NocC4S2G2B8TransactionLibrary
      with NocC4S2G2B8RoutingConstraints
      with TableBasedInterferenceSpecification {}

  object NocC4S4G1B8
      extends NocC4S4G1B8Platform
      with NocC4S4G1B8Software
      with NocC4S4G1B8TransactionLibrary
      with NocC4S4G1B8RoutingConstraints
      with TableBasedInterferenceSpecification {}

  object NocC4S4G2B8
      extends NocC4S4G2B8Platform
      with NocC4S4G2B8Software
      with NocC4S4G2B8TransactionLibrary
      with NocC4S4G2B8RoutingConstraints
      with TableBasedInterferenceSpecification {}

  object NocC8S2G1B8
      extends NocC8S2G1B8Platform
      with NocC8S2G1B8Software
      with NocC8S2G1B8TransactionLibrary
      with NocC8S2G1B8RoutingConstraints
      with TableBasedInterferenceSpecification {}

  object NocC8S2G2B8
      extends NocC8S2G2B8Platform
      with NocC8S2G2B8Software
      with NocC8S2G2B8TransactionLibrary
      with NocC8S2G2B8RoutingConstraints
      with TableBasedInterferenceSpecification {}

  object NocC8S4G1B8
      extends NocC8S4G1B8Platform
      with NocC8S4G1B8Software
      with NocC8S4G1B8TransactionLibrary
      with NocC8S4G1B8RoutingConstraints
      with TableBasedInterferenceSpecification {}

  object NocC8S4G2B8
      extends NocC8S4G2B8Platform
      with NocC8S4G2B8Software
      with NocC8S4G2B8TransactionLibrary
      with NocC8S4G2B8RoutingConstraints
      with TableBasedInterferenceSpecification {}


  private val platforms = Seq(
    HbusCl2C2B8,
    HbusCl2C4B8,
    HbusCl4C2B8,
    DbusC2D2B8,
    DbusC2D4B8,
    DbusC2D8B8,
    DbusC4D2B8,
    DbusC4D4B8,
    DbusC4D8B8,
    DbusC8D2B8,
    DbusC8D4B8,
    NocC4S2G1B8,
    NocC4S2G2B8,
    NocC4S4G1B8,
    NocC4S4G2B8,
    NocC8S2G1B8,
    NocC8S2G2B8,
    NocC8S4G1B8,
    NocC8S4G2B8
  )

  "Generated architectures" should "be exportable" in {
    for {p <- platforms} {
      p.exportRestrictedHWAndSWGraph()
      p.exportDataAllocationTable()
      p.exportUserTransactions()
      p.exportPhysicalTransactions()
      p.exportUserScenarios()
      println(s"[INFO] exporting ${p.name.name} done")
    }
  }

  it should "be possible to compute the interference" in {
    for { p <- platforms } {
      p.computeAllInterference(
        1 hour,
        ignoreExistingAnalysisFiles = true,
        computeSemantics = false
      )
    }
  }
}
