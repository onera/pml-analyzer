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

package onera.pmlanalyzer.pml.experiments.hbus

import onera.pmlanalyzer.pml.model.hardware.*
import onera.pmlanalyzer.pml.operators.*
import sourcecode.Name

class HbusClXCYBYPlatform(name: Symbol, clusterNumber: Int, coreNumber: Int)
    extends Platform(name) {
  def this(clusterNbr: Int, coreNbr: Int)(implicit implicitName: Name) = {
    this(Symbol(implicitName.value), clusterNbr, coreNbr)
  }

  object cg0 extends Composite {

    case class Cluster(id: Int) extends Composite(Symbol(s"Cl$id")) {
      val cores: Seq[Initiator] =
        for { i <- 0 until coreNumber } yield Initiator(s"C$i")

      val coresL1: Seq[Target] =
        for { i <- 0 until coreNumber } yield Target(s"C${i}_L1")

      val bus: SimpleTransporter = SimpleTransporter()

      val L2: Target = Target()

      val input_port: SimpleTransporter = SimpleTransporter()

      val output_port: SimpleTransporter = SimpleTransporter()

      for { core <- cores }
        core link bus

      for { (core, l1) <- cores.zip(coresL1) }
        core link l1

      bus link L2

      input_port link bus

      bus link output_port

    }

    val clusters: Seq[Cluster] =
      for { i <- 0 until clusterNumber } yield Cluster(i)

    val L0_0: SimpleTransporter = SimpleTransporter()

    val input_port: SimpleTransporter = SimpleTransporter()

    val output_port: SimpleTransporter = SimpleTransporter()

    for { cluster <- clusters } {
      L0_0 link cluster.input_port
      cluster.output_port link L0_0
    }

    input_port link L0_0

    L0_0 link output_port
  }

  object ddr extends Composite {
    val banks: Seq[Target] =
      for { i <- 0 until clusterNumber } yield Target(s"BK$i")

    val ddr_ctrl: SimpleTransporter = SimpleTransporter()

    val input_port: SimpleTransporter = SimpleTransporter()

    for { bank <- banks }
      ddr_ctrl link bank

    input_port link ddr_ctrl
  }

  object cfg_bus extends Composite {
    val bus: SimpleTransporter = SimpleTransporter()

    val input_port: SimpleTransporter = SimpleTransporter()

    val dma_reg: Target = Target()

    val spi_reg: Target = Target()

    input_port link bus

    bus link dma_reg

    bus link spi_reg
  }

  val pf_bus: SimpleTransporter = SimpleTransporter()
  val eth: Target = Target()
  val dma: Initiator = Initiator()

  pf_bus link ddr.input_port

  pf_bus link cfg_bus.input_port

  pf_bus link cg0.input_port

  cg0.output_port link pf_bus

  dma link pf_bus

  pf_bus link eth
}
