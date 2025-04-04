package onera.pmlanalyzer.pml.experiments

import onera.pmlanalyzer.pml.model.hardware.{Composite, Initiator, Platform, SimpleTransporter, Target}
import onera.pmlanalyzer.pml.model.utils.ReflexiveInfo
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
      n: Symbol,
      nbGrpDSP: Int,
      nbGrpCore: Int,
      nbClGrpDSP: Int,
      nbClGrpCore: Int,
      nbClDSPPerGrp: Int,
      nbClCorePerGrp: Int,
      nbDSPPerCl: Int,
      nbCorePerCl: Int,
      nbDDRBk: Int,
      nbDDRCtrl: Int,
      dummy: Int = 0
  )(using givenLine: Line, givenFile: File) = {
    this(
      n,
      nbGrpDSP,
      nbGrpCore,
      nbClGrpDSP,
      nbClGrpCore,
      nbClDSPPerGrp,
      nbClCorePerGrp,
      nbDSPPerCl,
      nbCorePerCl,
      nbDDRBk,
      nbDDRCtrl,
      givenLine,
      givenFile
    )
  }

  sealed abstract class Cluster(n: Symbol, clusterInfo:ReflexiveInfo)
      extends Composite(n, clusterInfo) {
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

  final class ClusterCore private (
                                    val id: String,
                                    clusterCoreInfo:ReflexiveInfo
  ) extends Cluster(Symbol(s"ClC$id"), clusterCoreInfo) {

    def this(ident: String, dummy: Int = 0)(using
        givenInfo: ReflexiveInfo
    ) = {
      this(ident, givenInfo)
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

  final class ClusterDSP private (
      val id: String,
      clusterDSPInfo: ReflexiveInfo
  ) extends Cluster(Symbol(s"ClD$id"), clusterDSPInfo) {

    def this(ident: String, dummy: Int = 0)(using
        givenInfo: ReflexiveInfo
    ) = {
      this(ident, givenInfo)
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
      groupCrossBarInfo:ReflexiveInfo
  ) extends Composite(s"group_bus$id", groupCrossBarInfo) {

    def this(ident: Int, nbCl: Int, nbGr: Int, dummy: Int = 0)(using
        givenInfo: ReflexiveInfo
    ) = {
      this(ident, nbCl, nbGr, givenInfo)
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
      groupInfo: ReflexiveInfo
  ) extends Composite(s"cg$id", groupInfo) {
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

  final class GroupDSP private (
      val id: Int,
      groupDSPInfo: ReflexiveInfo
  ) extends Group[ClusterDSP](
        id,
        nbClusterDSPPerGroup,
        nbClusterGroupDSP,
        groupDSPInfo
      ) {

    def this(ident: Int, dummy: Int = 0)(using
        givenInfo: ReflexiveInfo
    ) = {
      this(ident, givenInfo)
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

  final class GroupCore private (
      val id: Int,
      groupCoreInfo: ReflexiveInfo
  ) extends Group[ClusterCore](
        id,
        nbClusterCorePerGroup,
        nbClusterGroupCore,
        groupCoreInfo
      ) {

    def this(ident: Int, dummy: Int = 0)(using
        givenInfo: ReflexiveInfo
    ) = {
      this(ident, givenInfo)
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

  final class DDR private (val id: Int, ddrInfo: ReflexiveInfo)
      extends Composite(s"ddr$id", ddrInfo) {

    def this(ident: Int, dummy: Int = 0)(using
        givenInfo: ReflexiveInfo
    ) = {
      this(ident, givenInfo)
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

  object cfg_bus extends Composite("cfg_bus") {
    val bus: SimpleTransporter = SimpleTransporter()

    val input_port: SimpleTransporter = SimpleTransporter()

    val dma_reg: Target = Target()

    val spi_reg: Target = Target()

    input_port link bus

    bus link dma_reg

    bus link spi_reg
  }

  object PlatformCrossBar extends Composite("pf_bus_G") {
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

  val ddrs: Seq[DDR] = for { i <- 0 until nbDDRController } yield DDR(i)

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
