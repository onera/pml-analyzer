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

package synthetic

import onera.pmlanalyzer.*

import java.io.FileWriter
import scala.concurrent.ExecutionContext.Implicits.*
import scala.concurrent.TimeoutException
import scala.concurrent.duration.*
import scala.language.postfixOps
import scala.util.{Failure, Success, Try}

object GeneratedPlatformsTest extends App {

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
  private val cores = Seq(4, 8, 16)
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
    if 0 < coreCount + dspCount
    if coreCount + dspCount <= 16
    if coresPerBankPerPartition <= 8
  } yield {
    println(
      s"[Experiments] generating: GenericSample_${coreCount}Cores_${clusterCount}Cl_${dspCount}Dsp_${ddrPartitions}Prt_${coresPerBankPerPartition}CorePerBank${
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

  final case class ExperimentResults(
      nbInitiators: Int,
      nbTargets: Int,
      nbTransactions: Int,
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
        s"$nbInitiators, $nbTargets, $nbTransactions, $printTime, $semanticsSize, $pintGraphRed, $printSemRed, "
      )
      for { i <- 2 to maxSemantics }
        writer.write(
          s"${semanticsDistribution.get(i).map(_.toString).getOrElse("0")}, "
        )

      for { i <- 2 to maxItf }
        writer.write(
          s"${itfDistribution.get(i).map(_.toString).getOrElse("0")}, "
        )

      for { i <- 2 to maxFree }
        writer.write(
          s"${freeDistribution.get(i).map(_.toString).getOrElse("0")}, "
        )

      for { i <- 2 to maxRed }
        writer.write(
          s"${redDistribution.get(i).map(_.toString).getOrElse("0")} ${
              if (i == maxRed) "" else ", "
            }"
        )
  }

  def getMaxMultiTransactionSize(results: Seq[Set[Int]]): Int =
    results.filter(_.nonEmpty).map(_.max).max

  val timeout: Duration = (1 days)
  println(timeout)
  for {
    p <- platforms
  } {
    Try {
      p.computeAllInterference(
        timeout,
        onlySummary = true
      )
    } match
      case Success(_) =>
        println(s"[INFO] exporting ${p.name.name} done")
      case Failure(_: TimeoutException) =>
        println(
          s"[ERROR] Timeout (after $timeout) for analysis of ${p.fullName}"
        )
      case Failure(_) =>
        println(s"[ERROR] Unknown error during analysis of ${p.fullName}")
  }

  val resultFile = FileManager.exportDirectory.getFile("experiments.csv")
  val writer = new FileWriter(resultFile)
  val result =
    (for {
      p <- platforms
    } yield {
      val semanticsDistribution = p.getSemanticsSize()

      val (itf, free, analysisTime) =
        p.parseSummaryFile(
          Some(Method.Default),
          Some(SolverImplm.Monosat)
        ) match
          case Some(value) => (value._1, value._2, Some(value._3))
          case None => (Map.empty[Int, BigInt], Map.empty[Int, BigInt], None)

      val semanticsReduction =
        if (itf.nonEmpty || free.nonEmpty)
          Some(p.computeSemanticReduction(SolverImplm.Monosat, Method.Default))
        else None
      val graphReduction =
        if (itf.nonEmpty || free.nonEmpty)
          Some(p.computeGraphReduction(SolverImplm.Monosat, Method.Default))
        else None

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

  val maxItfSize =
    getMaxMultiTransactionSize(result.map(_._2.itfDistribution.keySet))
  val maxFreeSize =
    getMaxMultiTransactionSize(result.map(_._2.freeDistribution.keySet))
  val maxRedSize =
    getMaxMultiTransactionSize(result.map(_._2.redDistribution.keySet))
  val maxSemanticsSize =
    getMaxMultiTransactionSize(result.map(_._2.semanticsDistribution.keySet))
  writer.write(
    "platform, nbInitiators, nbTargets, nbTransactions, analysisTime, semanticsSize, graphReduction, semanticsReduction, "
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
