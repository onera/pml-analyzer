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

import onera.pmlanalyzer.pml.exporters.*
import onera.pmlanalyzer.pml.model.configuration.TransactionLibrary
import onera.pmlanalyzer.pml.model.hardware.Platform
import onera.pmlanalyzer.pml.model.utils.Message
import onera.pmlanalyzer.pml.operators.*
import onera.pmlanalyzer.views.interference.InterferenceTestExtension
import onera.pmlanalyzer.views.interference.exporters.*
import onera.pmlanalyzer.views.interference.model.specification.{
  ApplicativeTableBasedInterferenceSpecification,
  PhysicalTableBasedInterferenceSpecification
}
import onera.pmlanalyzer.views.interference.operators.*
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should

import java.io.FileWriter
import scala.collection.parallel.CollectionConverters.*
import scala.concurrent.ExecutionContext.Implicits.*
import scala.concurrent.duration.*
import scala.concurrent.{Await, Future, TimeoutException}
import scala.language.postfixOps
import scala.util.{Failure, Success, Try}
import InterferenceTestExtension.PerfTests

class GeneratedPlatformsTest extends AnyFlatSpec with should.Matchers {

  def generatePlatformFromConfiguration(
      coreCount: Int,
      clusterCount: Int,
      dspCount: Int,
      ddrPartitions: Int,
      coresPerBankPerPartition: Int,
      withDMA: Boolean = true
  ): Platform
    with TransactionLibrary
    with PhysicalTableBasedInterferenceSpecification
    with ApplicativeTableBasedInterferenceSpecification = {
    // FIXME Assert ddrPartitions <= GP Core Count
    // FIXME Assert GP Core count is multiple of ddrPartitions
    // FIXME Assert GP Core count is multiple of ddrPartitions

    // Create a GP group per partition
    // FIXME How to configure more than 1 GP cluster per group?
    val gp_group_count: Int = clusterCount
    val gp_cluster_per_group: Int = 1
    val gp_cores_per_cluster = coreCount / gp_group_count / gp_cluster_per_group

    // Allocate banks to groups and cores
    val bank_count: Int =
      Math.ceil(coreCount / ddrPartitions / coresPerBankPerPartition).toInt

    // Create a single DSP group
    // - No activity from DSP Cores outside their cluster/group
    // - Only the (single) DMA comes into the DSP clusters
    val dsp_group_count: Int = 1
    val dsp_cluster_per_group: Int = 1
    val dsp_cores_per_cluster: Int =
      dspCount / dsp_group_count / dsp_cluster_per_group

    // Derive additional Generic Platform parameters
    val name = Symbol(
      s"GenericSample_${coreCount}Cores_${clusterCount}Cl_${dspCount}Dsp_${ddrPartitions}Prt_${coresPerBankPerPartition}CorePerBank${
          if withDMA then "" else "_noDMA"
        }"
    )
    val ddr_count: Int = ddrPartitions

    new GenericPlatform(
      n = name,
      nbGrpCore = gp_group_count,
      nbGrpDSP = dsp_group_count,
      nbClGrpDSP = 1, // FIXME This input seems redundant with the DSP Per Group
      nbClGrpCore =
        1, // FIXME This input seems redundant with the Core Per Group
      nbClCorePerGrp = gp_cluster_per_group,
      nbClDSPPerGrp = dsp_cluster_per_group,
      nbCorePerCl = gp_cores_per_cluster,
      nbDSPPerCl = dsp_cores_per_cluster,
      nbDDRBk = bank_count,
      nbDDRCtrl = ddr_count
    ) with GenericSoftware
      with GenericTransactionLibrary(withDMA)
      with GenericRoutingConstraints
      with GenericTransactionInterferenceSpecification
      with GenericApplicationInterferenceSpecification
  }

  val log2 = (x: Int) => (Math.log10(x) / Math.log10(2.0)).toInt
  private val cores = Seq(2, 4, 8, 16)
  private val dsps = Seq(0)
  private lazy val platforms: Seq[
    Platform & TransactionLibrary &
      PhysicalTableBasedInterferenceSpecification &
      ApplicativeTableBasedInterferenceSpecification
  ] = for {
    coreCount <- cores
    dspCount <- dsps

    clusterCount <- {
      for { i <- 0 to log2(coreCount) } yield {
        Math.pow(2.0, i).toInt
      }
    }
    ddrPartitions <- {
      for { i <- 0 to Math.min(log2(clusterCount), 1) } yield {
        Math.pow(2.0, i).toInt
      }
    }
    coresPerBankPerPartition <- {
      for {
        i <- 0 to log2(
          (clusterCount / ddrPartitions) * (coreCount / clusterCount)
        )
      } yield {
        Math.pow(2.0, i).toInt
      }
    }
    withDMA <- Seq(false)
    if (0 < coreCount + dspCount)
    if (coreCount + dspCount <= 8)
    if (coresPerBankPerPartition <= 8)
  } yield {
    println(
      s"[TEST] generating: GenericSample_${coreCount}Cores_${clusterCount}Cl_${dspCount}Dsp_${ddrPartitions}Prt_${coresPerBankPerPartition}CorePerBank${
          if withDMA then "" else "_noDMA"
        }"
    )
    generatePlatformFromConfiguration(
      coreCount = coreCount,
      clusterCount = clusterCount,
      dspCount = dspCount,
      ddrPartitions = ddrPartitions,
      coresPerBankPerPartition = coresPerBankPerPartition,
      withDMA = withDMA
    )
  }

  "For specific architecture it" should "be possible to compute interference" taggedAs PerfTests in {
    val timeout = (1 hour)
    val p = generatePlatformFromConfiguration(
      coreCount = 4,
      clusterCount = 2,
      dspCount = 0,
      ddrPartitions = 1,
      coresPerBankPerPartition = 1,
      withDMA = false
    )
    p.exportRestrictedHWAndSWGraph()
    p.computeAllInterference(
      timeout
    )
  }

  "Generated architectures" should "be analysable to compute their semantics" taggedAs PerfTests in {
    val timeout = (1 minute)
    for {
      p <- platforms
      c = Try(Await.result(Future(p.exportSemanticsSize()), timeout))
    } {
      c match
        case Success(_) =>
          println(s"[TEST] exporting ${p.name.name} done")
        case Failure(_: TimeoutException) =>
          println(
            s"[TEST] Failure (after $timeout) for analysis of ${p.fullName}"
          )
        case Failure(_) =>
          println(s"[TEST] Unknown error during analysis of ${p.fullName}")
    }
  }

  it should "be possible to export the HW and SW graph" taggedAs PerfTests in {
    for {
      p <- platforms
    } {
      p.exportRestrictedHWAndSWGraph()
      p.exportUserTransactions()
    }
  }

  it should "be possible to compute the interference" taggedAs PerfTests in {
    assume(
      InterferenceTestExtension.monosatLibraryLoaded,
      Message.monosatLibraryNotLoaded
    )
    val timeout: Duration = (1 days)
    println(timeout)
    for {
      p <- platforms
      if FileManager.exportDirectory
        .locate(FileManager.getSemanticSizeFileName(p))
        .isDefined
    } {
      p.exportGraphReduction()
      (Try {
        p.computeAllInterference(
          timeout,
          onlySummary = true
        )
        p.exportSemanticReduction()
      }) match
        case Success(_) =>
          println(s"[TEST] exporting ${p.name.name} done")
        case Failure(_: TimeoutException) =>
          println(
            s"[TEST] Timeout (after $timeout) for analysis of ${p.fullName}"
          )
        case Failure(_) =>
          println(s"[TEST] Unknown error during analysis of ${p.fullName}")
    }
  }

  final case class ExperimentResults(
      nbInitiators: Int,
      nbTargets: Int,
      nbScenarios: Int,
      analysisTime: Option[Double],
      semanticsDistribution: Map[Int, BigInt],
      itfDistribution: Map[Int, BigInt],
      freeDistribution: Map[Int, BigInt],
      graphReduction: Option[BigDecimal],
      semanticsReduction: Option[BigDecimal]
  ) {
    val semanticsSize: BigInt =
      if (semanticsDistribution.nonEmpty)
        semanticsDistribution.values.sum
      else 0
    val redDistribution: Map[Int, BigInt] =
      for {
        (k, v) <- semanticsDistribution
        if freeDistribution.contains(k) || itfDistribution.contains(k)
      } yield k -> (v - freeDistribution.getOrElse(k, 0) - itfDistribution
        .getOrElse(k, 0))

    def printWith(
        writer: FileWriter,
        maxSemantics: Int,
        maxItf: Int,
        maxFree: Int,
        maxRed: Int
    ): Unit =
      val printTime =
        analysisTime match
          case Some(value) => value.toString
          case None        => "none"
      val printSemRed =
        semanticsReduction match
          case Some(value) if value == -1 => "inf"
          case Some(value)                => value.toString
          case None                       => "none"
      val pintGraphRed =
        graphReduction match
          case Some(value) if value == -1 => "inf"
          case Some(value)                => value.toString
          case None                       => "none"
      writer.write(
        s"$nbInitiators, $nbTargets, $nbScenarios, $printTime, $semanticsSize, $pintGraphRed, $printSemRed, "
      )
      for { i <- 2 to maxSemantics }
        writer.write(
          s"${semanticsDistribution.get(i).map(_.toString).getOrElse("none")}, "
        )

      for { i <- 2 to maxItf }
        writer.write(
          s"${itfDistribution.get(i).map(_.toString).getOrElse("none")}, "
        )

      for { i <- 2 to maxFree }
        writer.write(
          s"${freeDistribution.get(i).map(_.toString).getOrElse("none")}, "
        )

      for { i <- 2 to maxRed }
        writer.write(
          s"${redDistribution.get(i).map(_.toString).getOrElse("none")} ${
              if (i == maxRed) "" else ", "
            }"
        )
  }

  def getMaxScenarioSize(results: Seq[Set[Int]]): Int =
    results.filter(_.nonEmpty).map(_.max).max

  it should "be used to export performance plots" taggedAs PerfTests in {
    val resultFile = FileManager.exportDirectory.getFile("experiments.csv")
    val writer = new FileWriter(resultFile)
    val result =
      (for {
        p <- platforms
        if FileManager.exportDirectory
          .locate(FileManager.getSemanticSizeFileName(p))
          .isDefined
      } yield {
        val semanticsDistribution = p.getSemanticsSize()

        val (itf, free, analysisTime) = PostProcess.parseSummaryFile(p) match
          case Some(value) => (value._1, value._2, Some(value._3))
          case None => (Map.empty[Int, BigInt], Map.empty[Int, BigInt], None)

        val semanticsReduction =
          if (itf.nonEmpty) Some(p.computeSemanticReduction()) else None
        val graphReduction =
          if (itf.nonEmpty) Some(p.computeGraphReduction()) else None

        p.fullName -> ExperimentResults(
          p.initiators.size,
          p.targets.size,
          p.transactionByUserName.keySet.size,
          analysisTime,
          semanticsDistribution,
          itf,
          free,
          graphReduction,
          semanticsReduction
        )
      }).sortBy(_._1)

    val maxItfSize = getMaxScenarioSize(result.map(_._2.itfDistribution.keySet))
    val maxFreeSize =
      getMaxScenarioSize(result.map(_._2.freeDistribution.keySet))
    val maxRedSize = getMaxScenarioSize(result.map(_._2.redDistribution.keySet))
    val maxSemanticsSize =
      getMaxScenarioSize(result.map(_._2.semanticsDistribution.keySet))
    writer.write(
      "platform, nbInitiators, nbTargets, nbScenarios, analysisTime, semanticsSize, graphReduction, semanticsReduction, "
    )
    writer.write(
      (2 to maxSemanticsSize).map(i => s"sem size $i").mkString("", ",", ",")
    )
    writer.write(
      (2 to maxItfSize).map(i => s"itf size $i").mkString("", ",", ",")
    )
    writer.write(
      (2 to maxFreeSize).map(i => s"free size $i").mkString("", ",", ",")
    )
    writer.write((2 to maxRedSize).map(i => s"red size $i").mkString(","))
    writer.write("\n")

    for {
      (p, r) <- result
    } {
      writer.write(s"$p, ")
      r.printWith(writer, maxSemanticsSize, maxItfSize, maxFreeSize, maxRedSize)
      writer.write("\n")
    }
    writer.flush()
    writer.close()
  }
}
