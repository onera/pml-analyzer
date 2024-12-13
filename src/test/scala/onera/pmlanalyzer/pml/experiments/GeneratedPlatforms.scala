/*******************************************************************************
 * Copyright (c)  2023. ONERA
 * This file is part of PML Analyzer
 *
 * PML Analyzer is free software ;
 * you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation ;
 * either version 2 of  the License, or (at your option) any later version.
 *
 * PML Analyzer is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY ;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this program ;
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA
 ******************************************************************************/

package onera.pmlanalyzer.pml.experiments

import onera.pmlanalyzer.GnuPlotWriter
import onera.pmlanalyzer.pml.experiments.hbus.*
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
      with TableBasedInterferenceSpecification {}

  object HbusCl2C4B8
      extends HbusCl2C4B8Platform
      with HbusCl2C4B8Software
      with HbusCl2C4B8TransactionLibrary
      with TableBasedInterferenceSpecification {}

  object HbusCl2C8B8
      extends HbusCl2C8B8Platform
      with HbusCl2C8B8Software
      with HbusCl2C8B8TransactionLibrary
      with TableBasedInterferenceSpecification {}

  object HbusCl4C2B8
      extends HbusCl4C2B8Platform
      with HbusCl4C2B8Software
      with HbusCl4C2B8TransactionLibrary
      with TableBasedInterferenceSpecification {}

  object HbusCl4C4B8
      extends HbusCl4C4B8Platform
      with HbusCl4C4B8Software
      with HbusCl4C4B8TransactionLibrary
      with TableBasedInterferenceSpecification {}

  object HbusCl4C8B8
      extends HbusCl4C8B8Platform
      with HbusCl4C8B8Software
      with HbusCl4C8B8TransactionLibrary
      with TableBasedInterferenceSpecification {}

  object HbusCl8C2B8
      extends HbusCl8C2B8Platform
      with HbusCl8C2B8Software
      with HbusCl8C2B8TransactionLibrary
      with TableBasedInterferenceSpecification {}

  object HbusCl8C4B8
      extends HbusCl8C4B8Platform
      with HbusCl8C4B8Software
      with HbusCl8C4B8TransactionLibrary
      with TableBasedInterferenceSpecification {}

  object HbusCl8C8B8
      extends HbusCl8C8B8Platform
      with HbusCl8C8B8Software
      with HbusCl8C8B8TransactionLibrary
      with TableBasedInterferenceSpecification {}

  private val platforms = Seq(
    HbusCl2C2B8,
    HbusCl2C4B8,
    HbusCl2C8B8,
    HbusCl4C2B8,
    HbusCl4C4B8,
    HbusCl4C8B8,
    HbusCl8C2B8,
    HbusCl8C4B8,
    HbusCl8C8B8
  )

  "Generated architectures" should "be exportable" in {
    for { p <- platforms } {
      p.exportRestrictedHWAndSWGraph()
      p.exportDataAllocationTable()
      p.exportUserTransactions()
      p.exportPhysicalTransactions()
      p.exportUserScenarios()
      p.exportSemanticsSize()
      println(s"[INFO] exporting ${p.name.name} done")
    }
  }

  it should "be possible to compute the interference" in {
    for { p <- platforms } {
      p.computeAllInterference(
        10 minutes,
        ignoreExistingAnalysisFiles = true
      )
    }
  }
}
