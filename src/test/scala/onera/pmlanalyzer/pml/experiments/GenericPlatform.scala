package onera.pmlanalyzer.pml.experiments

import onera.pmlanalyzer.pml.model.hardware.{
  Composite,
  Initiator,
  Platform,
  SimpleTransporter,
  Target
}
import onera.pmlanalyzer.pml.operators.*
import sourcecode.*

/*******************************************************************************
 * Copyright (c) 2025. ONERA
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
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA
 ******************************************************************************/

class GenericPlatform private (
    name: Symbol,
    nbGroupDSP: Int,
    nbGroupCore: Int,
    nbClusterGroupDSP: Int,
    nbClusterGroupCore: Int,
    nbClusterDSPPerGroup: Int,
    nbClusterCorePerGroup: Int,
    nbDSPPerCluster: Int,
    nbCorePerCluster: Int,
    nbDDRBank: Int,
    nbDDRController: Int,
    line: Line,
    file: File
) extends Platform(name, line, file) {

  def this(
      name: Symbol,
      nbGroupDSP: Int,
      nbGroupCore: Int,
      nbClusterGroupDSP: Int,
      nbClusterGroupCore: Int,
      nbClusterDSPPerGroup: Int,
      nbClusterCorePerGroup: Int,
      nbDSPPerCluster: Int,
      nbCorePerCluster: Int,
      nbDDRBank: Int,
      nbDDRController: Int,
      dummy: Int = 0
  )(using givenLine: Line, givenFile: File) = {
    this(
      name,
      nbGroupDSP,
      nbGroupCore,
      nbClusterGroupDSP,
      nbClusterGroupCore,
      nbClusterDSPPerGroup,
      nbClusterCorePerGroup,
      nbDSPPerCluster,
      nbCorePerCluster,
      nbDDRBank,
      nbDDRController,
      givenLine,
      givenFile
    )
  }

  sealed abstract class Cluster(n: Symbol, line: Line, file: File)
      extends Composite(n, line, file) {
    val cores: Seq[Initiator]
    val input_port: SimpleTransporter = SimpleTransporter()
    val output_port: SimpleTransporter = SimpleTransporter()
    val bus: SimpleTransporter = SimpleTransporter()

    def prepare_topology(): Unit = {
      for { core <- cores }
        core link bus

      input_port link bus

      bus link output_port
    }

  }

  final class ClusterCore private (val id: String, line: Line, file: File)
      extends Cluster(Symbol(s"ClC$id"), line, file) {

    def this(id: String, dummy: Int = 0)(using
        givenLine: Line,
        givenFile: File
    ) = {
      this(id, givenLine, givenFile)
    }

    val cores: Seq[Initiator] =
      for { i <- 0 until nbCorePerCluster } yield Initiator(s"C$i")

    val coresL1: Seq[Target] =
      for { i <- 0 until nbCorePerCluster } yield Target(s"C${i}_L1")

    val L2: Target = Target()

    prepare_topology()

    for { (core, l1) <- cores.zip(coresL1) }
      core link l1

    bus link L2
  }

  final class ClusterDSP private (val id: String, line: Line, file: File)
      extends Cluster(Symbol(s"ClD$id"), line, file) {

    def this(id: String, dummy: Int = 0)(using
        givenLine: Line,
        givenFile: File
    ) = {
      this(id, givenLine, givenFile)
    }

    val cores: Seq[Initiator] =
      for { i <- 0 until nbDSPPerCluster } yield Initiator(s"C$i")

    val SRAM: Seq[Target] =
      for { i <- 0 until nbDSPPerCluster } yield Target(s"C${i}_SRAM")

    prepare_topology()

    for { sram <- SRAM }
      bus link sram

    for { (core, sram) <- cores.zip(SRAM) }
      core link sram
  }

  final class GroupCrossBar private (
      val id: Int,
      nbCluster: Int,
      nbGroup: Int,
      line: Line,
      file: File
  ) extends Composite(s"group_bus$id", line, file) {

    def this(id: Int, nbCluster: Int, nbGroup: Int, dummy: Int = 0)(using
        givenLine: Line,
        givenFile: File
    ) = {
      this(id, nbCluster, nbGroup, givenLine, givenFile)
    }

    val clusterInput: Seq[Seq[SimpleTransporter]] =
      for {
        i <- 0 until nbCluster
      } yield {
        for {
          j <- 0 until nbGroup
        } yield {
          SimpleTransporter(s"input_port_Cl${i}_$j")
        }
      }

    val clusterOuput: Seq[Seq[SimpleTransporter]] =
      for {
        i <- 0 until nbCluster
      } yield {
        for {
          j <- 0 until nbGroup
        } yield {
          SimpleTransporter(s"output_port_Cl${i}_$j")
        }
      }

    val input_port: SimpleTransporter = SimpleTransporter()
    val output_port: SimpleTransporter = SimpleTransporter()

    for {
      input <- clusterInput.flatten :+ input_port
      output <- clusterOuput.flatten :+ output_port
    } {
      input link output
    }
  }

  sealed abstract class Group[+T <: Cluster](
      id: Int,
      nbCluster: Int,
      nbClusterGroup: Int,
      line: Line,
      file: File
  ) extends Composite(s"cg$id", line, file) {
    val clusters: Seq[Seq[T]]

    val bus: GroupCrossBar = GroupCrossBar(id, nbCluster, nbClusterGroup)
    val output_port: SimpleTransporter = SimpleTransporter()
    val input_port: SimpleTransporter = SimpleTransporter()

    def prepare_topology(): Unit = {
      for {
        i <- clusters.indices
        j <- clusters(i).indices
      } {
        clusters(i)(j).output_port link bus.clusterInput(i)(j)
        bus.clusterOuput(i)(j) link clusters(i)(j).input_port
      }

      bus.output_port link output_port
      input_port link bus.input_port

    }

  }

  final class GroupDSP private (val id: Int, line: Line, file: File)
      extends Group[ClusterDSP](
        id,
        nbClusterDSPPerGroup,
        nbClusterGroupDSP,
        line,
        file
      ) {

    def this(id: Int, dummy: Int = 0)(using
        givenLine: Line,
        givenFile: File
    ) = {
      this(id, givenLine, givenFile)
    }

    val clusters: Seq[Seq[ClusterDSP]] =
      for {
        i <- 0 until nbClusterDSPPerGroup
      } yield {
        for {
          j <- 0 until nbClusterGroupDSP
        } yield ClusterDSP(s"G${id}_${i}_$j")
      }

    prepare_topology()
  }

  final class GroupCore private (val id: Int, line: Line, file: File)
      extends Group[ClusterCore](
        id,
        nbClusterCorePerGroup,
        nbClusterGroupCore,
        line,
        file
      ) {

    def this(id: Int, dummy: Int = 0)(using
        givenLine: Line,
        givenFile: File
    ) = {
      this(id, givenLine, givenFile)
    }

    val clusters: Seq[Seq[ClusterCore]] =
      for {
        i <- 0 until nbClusterCorePerGroup
      } yield {
        for {
          j <- 0 until nbClusterGroupCore
        } yield ClusterCore(s"G${id}_${i}_$j")
      }

    prepare_topology()
  }

  final class ddr private (val id: Int, line: Line, file: File)
      extends Composite(s"ddr$id", line, file) {

    def this(id: Int, dummy: Int = 0)(using
        givenLine: Line,
        givenFile: File
    ) = {
      this(id, givenLine, givenFile)
    }

    val banks: Seq[Target] =
      for { i <- 0 until nbDDRBank } yield Target(s"BK$i")

    val ddr_ctrl: SimpleTransporter = SimpleTransporter()

    val input_port: SimpleTransporter = SimpleTransporter()

    for { bank <- banks }
      ddr_ctrl link bank

    input_port link ddr_ctrl
  }

  val groupDSP: Seq[GroupDSP] = for {
    i <- 0 until nbGroupDSP
  } yield {
    GroupDSP(i)
  }

  val groupCore: Seq[GroupCore] = for {
    i <- 0 until nbGroupCore
  } yield {
    GroupCore(i + nbGroupDSP)
  }

  object cfg_bus extends Composite(name) {
    val bus: SimpleTransporter = SimpleTransporter()

    val input_port: SimpleTransporter = SimpleTransporter()

    val dma_reg: Target = Target()

    val spi_reg: Target = Target()

    input_port link bus

    bus link dma_reg

    bus link spi_reg
  }

  object PlatformCrossBar extends Composite(s"pf_bus_G") {
    val groupDSPInputPorts: Seq[SimpleTransporter] =
      for { g <- groupDSP } yield SimpleTransporter(s"dG${g.id}_input_port")

    val groupDSPOutputPorts: Seq[SimpleTransporter] =
      for { g <- groupDSP } yield SimpleTransporter(s"dG${g.id}_output_port")

    val groupCoreInputPorts: Seq[SimpleTransporter] =
      for { g <- groupCore } yield SimpleTransporter(s"G${g.id}_input_port")

    val groupCoreOutputPorts: Seq[SimpleTransporter] =
      for { g <- groupCore } yield SimpleTransporter(s"G${g.id}_output_port")

    val ddrOutputPorts: Seq[SimpleTransporter] =
      for { i <- 0 until nbDDRController } yield SimpleTransporter(
        s"ddr${i}_output_port"
      )

    val dma_input_port: SimpleTransporter = SimpleTransporter()
    val config_bus_output_port: SimpleTransporter = SimpleTransporter()
    val eth_output_port: SimpleTransporter = SimpleTransporter()

    for {
      input <- groupDSPInputPorts ++ groupCoreInputPorts :+ dma_input_port
      output <-
        groupDSPOutputPorts ++ groupCoreOutputPorts ++ ddrOutputPorts :+ config_bus_output_port :+ eth_output_port
    } {
      input link output
    }
  }

  val ddrs: Seq[ddr] = for { i <- 0 until nbDDRController } yield ddr(i)

  val dma: Initiator = Initiator()

  val eth: Target = Target()

  for { i <- groupDSP.indices } {
    groupDSP(i).output_port link PlatformCrossBar.groupDSPInputPorts(i)
    PlatformCrossBar.groupDSPOutputPorts(i) link groupDSP(i).input_port
  }

  for { i <- groupCore.indices } {
    groupCore(i).output_port link PlatformCrossBar.groupCoreInputPorts(i)
    PlatformCrossBar.groupCoreOutputPorts(i) link groupCore(i).input_port
  }

  for { i <- ddrs.indices } {
    PlatformCrossBar.ddrOutputPorts(i) link ddrs(i).input_port
  }

  dma link PlatformCrossBar.dma_input_port

  PlatformCrossBar.eth_output_port link eth

  PlatformCrossBar.config_bus_output_port link cfg_bus.input_port
}
