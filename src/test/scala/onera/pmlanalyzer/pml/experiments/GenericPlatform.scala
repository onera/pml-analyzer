package onera.pmlanalyzer.pml.experiments

import onera.pmlanalyzer.pml.model.hardware.*
import onera.pmlanalyzer.pml.model.utils.{Context, ReflexiveInfo}
import onera.pmlanalyzer.*
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

  val groupDSP: Seq[GroupDSP] = for {
    i <- 0 until nbGroupDSP
  } yield {
    GroupDSP(i, nbClusterDSPPerGroup, nbClusterGroupDSP, nbDSPPerCluster)
  }

  val groupCore: Seq[GroupCore] = for {
    i <- 0 until nbGroupCore
  } yield {
    GroupCore(
      i + nbGroupDSP,
      nbClusterCorePerGroup,
      nbClusterGroupCore,
      nbCorePerCluster
    )
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

  val ddrs: Seq[DDR] =
    for { i <- 0 until nbDDRController } yield DDR(i, nbDDRBank)

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
