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

abstract class GenericPlatform(name: Symbol) extends Platform(name) {

  sealed abstract class Cluster(n: Symbol) extends Composite(n) {
    val input_port: SimpleTransporter = SimpleTransporter()
    val output_port: SimpleTransporter = SimpleTransporter()
    val bus: SimpleTransporter = SimpleTransporter()
  }

  final case class ClusterCore(id: String, coreNumber: Int)
      extends Cluster(Symbol(s"ClC$id")) {
    val cores: Seq[Initiator] =
      for { i <- 0 until coreNumber } yield Initiator(s"C$i")

    val coresL1: Seq[Target] =
      for { i <- 0 until coreNumber } yield Target(s"C${i}_L1")

    val L2: Target = Target()

    for { core <- cores }
      core link bus

    for { (core, l1) <- cores.zip(coresL1) }
      core link l1

    bus link L2

    input_port link bus

    bus link output_port
  }

  final case class ClusteDSP(id: String, dspNumber: Int)
      extends Cluster(Symbol(s"ClD$id")) {

    val cores: Seq[Initiator] =
      for { i <- 0 until dspNumber } yield Initiator(s"C$i")

    val SRAM: Seq[Target] =
      for { i <- 0 until dspNumber } yield Target(s"C${i}_SRAM")

    for { core <- cores }
      core link bus

    for { sram <- SRAM }
      bus link sram

    input_port link bus

    bus link output_port

    for { (core, sram) <- cores.zip(SRAM) }
      core link sram
  }

  sealed abstract class Group[T <: Cluster](
      id: Int,
      nbCluster: Int,
      nbGroup: Int,
      nbCore: Int,
      builder: (Int, Int, Int) => T
  ) extends Composite(s"cg$id") {
    val clusters: Seq[Seq[T]] =
      for {
        i <- 0 until nbCluster
      } yield {
        for {
          j <- 0 until nbGroup
        } yield builder(i, j, nbCore)
      }

    val bus: GroupCrossBar = GroupCrossBar(nbCluster, nbGroup)
    val output_port: SimpleTransporter = SimpleTransporter()
    val input_port: SimpleTransporter = SimpleTransporter()

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

  final case class ddr(bankNumber: Int) extends Composite("ddr") {

    val banks: Seq[Target] =
      for { i <- 0 until bankNumber } yield Target(s"BK$i")

    val ddr_ctrl: SimpleTransporter = SimpleTransporter()

    val input_port: SimpleTransporter = SimpleTransporter()

    for { bank <- banks }
      ddr_ctrl link bank

    input_port link ddr_ctrl
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

  final case class GroupCrossBar(nbCluster: Int, nbGroup: Int)
      extends Composite(s"group_bus") {
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

  final case class PlatformCrossBar(nbGroup: Int)
      extends Composite(s"pf_bus_G$nbGroup") {
    val groupInputPorts: Seq[SimpleTransporter] =
      for { i <- 0 until nbGroup } yield SimpleTransporter(s"G${i}_input_port")

    val groupOutputPorts: Seq[SimpleTransporter] =
      for { i <- 0 until nbGroup } yield SimpleTransporter(s"G${i}_output_port")

    val dma_input_port: SimpleTransporter = SimpleTransporter()
    val ddr_output_port: SimpleTransporter = SimpleTransporter()
    val config_bus_output_port: SimpleTransporter = SimpleTransporter()
    val eth_output_port: SimpleTransporter = SimpleTransporter()

    for {
      input <- groupInputPorts :+ dma_input_port
      output <-
        groupOutputPorts :+ ddr_output_port :+ config_bus_output_port :+ eth_output_port
    } {
      input link output
    }
  }
}
