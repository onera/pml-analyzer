/** *****************************************************************************
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
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA
 * **************************************************************************** */

package onera.pmlanalyzer.pml.experiments.dbus

import onera.pmlanalyzer.pml.model.hardware.{Composite, Initiator, Platform, SimpleTransporter, Target}
import sourcecode.Name
import onera.pmlanalyzer.pml.operators.*

class DbusCXDYBXPlatform(name: Symbol, coreNumber: Int, dspNumber: Int) extends Platform(name) {
  def this(cNbr: Int, dspNbr: Int)(implicit implicitName: Name) = {
    this(Symbol(implicitName.value), cNbr, dspNbr)
  }

  object rosace extends Composite {

    object cg0 extends Composite {

      object cl0 extends Composite {

        val cores: Seq[Initiator] =
          for {i <- 0 until coreNumber} yield Initiator(s"C$i")

        val bus: SimpleTransporter = SimpleTransporter()

        val input_port: SimpleTransporter = SimpleTransporter()

        val output_port: SimpleTransporter = SimpleTransporter()

        val L1Caches: Seq[Target] =
          for {i <- 0 until coreNumber} yield Target(s"C${i}_L1")

        val l2: Target = Target()

        for {core <- cores}
          core link bus

        input_port link bus

        bus link output_port

        for {(core, l1) <- cores.zip(L1Caches)} yield
          core link l1

        bus link l2
      }

      val input_port: SimpleTransporter = SimpleTransporter()

      val output_port: SimpleTransporter = SimpleTransporter()

      input_port link cl0.input_port

      cl0.output_port link output_port
    }

    object dg0 extends Composite {

      object cl0 extends Composite {

        val cores: Seq[Initiator] =
          for {i <- 0 until dspNumber} yield Initiator(s"C$i")

        val bus: SimpleTransporter = SimpleTransporter()

        val input_port: SimpleTransporter = SimpleTransporter()

        val output_port: SimpleTransporter = SimpleTransporter()

        val SRAM: Seq[Target] =
          for {i <- 0 until dspNumber} yield Target(s"C${i}_SRAM")

        for {core <- cores}
          core link bus

        //FIXME The DMA should be able to read SRAM, so a link to the bus is mandatory
        for {sram <- SRAM}
          bus link sram

        input_port link bus

        bus link output_port

        for {(core, sram) <- cores.zip(SRAM)}
          core link sram
      }

      val input_port: SimpleTransporter = SimpleTransporter()

      val output_port: SimpleTransporter = SimpleTransporter()

      input_port link cl0.input_port

      cl0.output_port link output_port
    }

    object ddr extends Composite {

      val banks: Seq[Target] =
        for {i <- 0 until coreNumber} yield Target(s"BK$i")

      val ddr_ctrl: SimpleTransporter = SimpleTransporter()

      val input_port: SimpleTransporter = SimpleTransporter()

      for {bank <- banks}
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

    val dma: Initiator = Initiator()

    val eth: Target = Target()

    val pf_bus: SimpleTransporter = SimpleTransporter()

    pf_bus link ddr.input_port

    pf_bus link cfg_bus.input_port

    pf_bus link cg0.input_port

    pf_bus link dg0.input_port

    cg0.output_port link pf_bus

    dg0.output_port link pf_bus

    dma link pf_bus

    pf_bus link eth
  }
}

