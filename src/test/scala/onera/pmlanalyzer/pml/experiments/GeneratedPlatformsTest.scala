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

import scala.concurrent.TimeoutException
import scala.concurrent.duration.*
import scala.io.Source
import scala.language.postfixOps

class GeneratedPlatformsTest extends AnyFlatSpec with should.Matchers {

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

  "Generated architectures" should "be analysable to compute their semantics" in {
    for {
      p <- platforms
    } {
      p.exportSemanticsSize()
      println(s"[TEST] exporting ${p.name.name} done")
    }
  }

  it should "be possible to compute the interference" in {
    val timeout: Duration = (1 days)
    println(timeout)
    for { p <- platforms } {
      p.exportGraphReduction()
      try {
        p.computeAllInterference(
          timeout,
          computeSemantics = false,
          onlySummary = true
        )
        p.exportSemanticReduction()
      } catch
        case _: TimeoutException =>
          println(
            s"[TEST] Timeout (after $timeout) for analysis of ${p.fullName}"
          )
        case _: InterruptedException =>
          println(s"[TEST] Interruption during analysis of ${p.fullName}")
        case _ =>
          println(s"[TEST] Unknown error during analysis of ${p.fullName}")
    }
  }

  final case class ExperimentResults(
                                      analysisTime: Double,
                                      semanticsDistribution: Map[Int, Int],
                                      itfDistribution: Map[Int, Int],
                                      freeDistribution: Map[Int, Int],
                                      graphReduction: Double,
                                      semanticsReduction: Double
                                    ) {
    val semanticsSize: BigInt = semanticsDistribution.values.sum
    val redDistribution: Map[Int, BigInt] = semanticsDistribution
      .transform((k, v) =>
        v - freeDistribution.getOrElse(k, 0) - itfDistribution.getOrElse(k, 0)
      )
  }

  it should "be used to export performance plots" in {
    val results = (for {
      p <- platforms
      if FileManager.exportDirectory
        .locate(FileManager.getSemanticSizeFileName(p))
        .isDefined
      (itf, free, analysisTime) <- PostProcess.parseSummaryFile(p)
      semanticsReduction = p.computeSemanticReduction()
      graphReduction = 1 // FIXME should be p.computeGraphReduction()
    } yield {
      val semanticsDistribution =
        p.getSemanticsSize().transform((_, v) => v.toInt)
      p.fullName -> ExperimentResults(
        analysisTime,
        semanticsDistribution,
        itf,
        free,
        graphReduction.toDouble,
        semanticsReduction.toDouble
      )
    }).toMap
    println(results)
  }
}
