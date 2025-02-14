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

import scala.concurrent.TimeoutException
import scala.concurrent.duration.*
import scala.io.Source
import scala.language.postfixOps

class DbusC2D2B8Test extends AnyFlatSpec with should.Matchers {

  object DbusC2D2B8
      extends DbusC2D2B8Platform
      with DbusC2D2B8Software
      with DbusC2D2B8RoutingConstraints
        with TableBasedInterferenceSpecification
        with DbusC2D2B8TransactionLibrary {}

  //FIXME WEIRD IN DEBUG MODE: ONE TEST NOT WORKS BUT ADDING THE SECOND AND THIRD TEST MAKES THE FIRST WORKS!
  "DbusC2D2B8" should "be analysable to compute semantics" in {
    DbusC2D2B8.exportPhysicalTransactions()
    DbusC2D2B8.exportUserScenarios()
    DbusC2D2B8.purifiedScenarios.size shouldEqual 40
    DbusC2D2B8.getSemanticsSize(ignoreExistingFile = true)(2) shouldEqual 600
  }

  //  "DbusC2D2B8 run 2" should "be analysable to compute semantics" in {
  //    DbusC2D2B8.purifiedScenarios.size shouldEqual 40
  //    DbusC2D2B8.getSemanticsSize(ignoreExistingFile = true)(2) shouldEqual 600
  //  }
  //
  //  "DbusC2D2B8 run 3" should "be analysable to compute semantics" in {
  //    DbusC2D2B8.purifiedScenarios.size shouldEqual 40
  //    DbusC2D2B8.getSemanticsSize(ignoreExistingFile = true)(2) shouldEqual 600
  //  }

  //  "DbusC2D2B8" should "be analysable to perform interference calculus" in {
  //    DbusC2D2B8.computeAllInterference(
  //      10 minutes,
  //      ignoreExistingAnalysisFiles = true,
  //      onlySummary = true
  //    )
  //    DbusC2D2B8.exportSemanticsSize(ignoreExistingFiles = true)
  //    DbusC2D2B8.exportAnalysisGraph()
  //    DbusC2D2B8.exportUserTransactions()
  //    DbusC2D2B8.exportUserScenarios()
  //    for {
  //      map <- PostProcess.parseSemanticsSizeFile(DbusC2D2B8)
  //    } yield {
  //      println(
  //        s"""
  //           |Computed Semantics:
  //           |${DbusC2D2B8.getSemanticsSize(ignoreExistingFile = true).mkString("\n")}
  //           |From file Semantics:
  //           |${map.mkString("\n")}
  //           |""".stripMargin
  //      )
  //    }
  //  }
}
