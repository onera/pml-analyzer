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
import onera.pmlanalyzer.views.interference.model.specification.{InterferenceSpecification, TableBasedInterferenceSpecification}
import onera.pmlanalyzer.views.interference.operators.*
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should

import java.io.FileWriter
import scala.concurrent.TimeoutException
import scala.concurrent.duration.*
import scala.io.Source
import scala.language.postfixOps

class GeneratedPlatformsTest extends AnyFlatSpec with should.Matchers {

  val dbusInstances: Seq[DbusCXDYBXPlatform
    & DbusCXDYBXSoftware
    & DbusCXDYBXTransactionLibrary
    & DbusCXDYBXRoutingConstraints
    & TableBasedInterferenceSpecification] =
    for {
      coreNumber <- 2 to 8 by 2
      dspNumber <- 2 to 8 by 2
      if coreNumber + dspNumber <= 8
    } yield {
      new DbusCXDYBXPlatform(Symbol(s"DbusC${coreNumber}D${dspNumber}B$coreNumber"), coreNumber, dspNumber)
        with DbusCXDYBXSoftware
        with DbusCXDYBXTransactionLibrary
        with DbusCXDYBXRoutingConstraints
        with TableBasedInterferenceSpecification {}
    }

  val hbusInstances: Seq[HbusClXCYBYPlatform
    & HbusClXCYBYSoftware
    & HbusClXCYBYTransactionLibrary
    & HbusClXCYBYRoutingConstraints
    & TableBasedInterferenceSpecification] =
    for {
      clusterNumber <- 2 to 8 by 2
      coreNumber <- 2 to 8 by 2
      if clusterNumber + coreNumber <= 12
    } yield {
      new HbusClXCYBYPlatform(Symbol(s"HbusCl${clusterNumber}C${coreNumber}B$coreNumber"), clusterNumber, coreNumber)
        with HbusClXCYBYSoftware
        with HbusClXCYBYTransactionLibrary
        with HbusClXCYBYRoutingConstraints
        with TableBasedInterferenceSpecification {}
    }

  private val platforms = dbusInstances

  "Generated architectures" should "be analysable to compute their semantics" in {
    for {
      p <- platforms
    } {
      p.exportSemanticsSize(ignoreExistingFiles = true)
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
                                      nbInitiators: Int,
                                      nbTargets: Int,
                                      nbScenarios: Int,
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

    def printWith(writer: FileWriter, maxSemantics: Int, maxItf: Int, maxFree: Int, maxRed: Int): Unit =
      writer.write(s"$nbInitiators, $nbTargets, $nbScenarios, $analysisTime, $semanticsSize, $graphReduction, $semanticsReduction")
      for {i <- 2 to maxSemantics}
        writer.write(s"${semanticsDistribution.getOrElse(i, 0)}, ")

      for {i <- 2 to maxItf}
        writer.write(s"${itfDistribution.getOrElse(i, 0)}, ")

      for {i <- 2 to maxFree}
        writer.write(s"${freeDistribution.getOrElse(i, 0)}, ")

      for {i <- 2 to maxRed}
        writer.write(s"${redDistribution.getOrElse(i, 0)} ${if (i == maxRed) "" else " ,"}")
  }

  it should "be used to export performance plots" in {
    val resultFile = FileManager.exportDirectory.getFile(s"experiments.csv")
    val writer = new FileWriter(resultFile)
    val result =
      (for {
      p <- platforms
      if FileManager.exportDirectory
        .locate(FileManager.getSemanticSizeFileName(p))
        .isDefined
      (itf, free, analysisTime) <- PostProcess.parseSummaryFile(p)
      semanticsReduction = p.computeSemanticReduction()
      graphReduction = p.computeGraphReduction()
      } yield {
        val semanticsDistribution = p.getSemanticsSize().transform((_, v) => v.toInt)

        p.fullName -> ExperimentResults(
          p.initiators.size,
          p.targets.size,
          p.scenarioByUserName.keySet.size,
          analysisTime,
          semanticsDistribution,
          itf,
          free,
          graphReduction.toDouble,
          semanticsReduction.toDouble)
      }).sortBy(_._1)

    val maxItfSize = result.map(_._2.itfDistribution.keySet.max).max
    val maxFreeSize = result.map(_._2.freeDistribution.keySet.max).max
    val maxRedSize = result.map(_._2.redDistribution.keySet.max).max
    val maxSemanticsSize = result.map(_._2.semanticsDistribution.keySet.max).max
    writer.write("platform, nbInitiators, nbTargets, nbScenarios, analysisTime, semanticsSize, graphReduction, semanticsReduction")
    writer.write((2 to maxSemanticsSize).map(i => s"semantics size $i").mkString("", ",", ","))
    writer.write((2 to maxItfSize).map(i => s"itf size $i").mkString("", ",", ","))
    writer.write((2 to maxFreeSize).map(i => s"itf size $i").mkString("", ",", ","))
    writer.write((2 to maxRedSize).map(i => s"itf size $i").mkString(","))
    writer.write("\n")

    for {
      (p, r) <- result
    } {
      writer.write(s"$p, ")
      r.printWith(writer, maxSemanticsSize, maxItfSize, maxFreeSize, maxRedSize)
      writer.write(s"\n")
    }
    writer.flush()
    writer.close()
  }
}
