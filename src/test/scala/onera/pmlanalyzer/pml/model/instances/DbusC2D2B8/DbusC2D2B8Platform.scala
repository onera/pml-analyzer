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

package onera.pmlanalyzer.pml.model.instances.DbusC2D2B8

import onera.pmlanalyzer.pml.model.hardware.*
import onera.pmlanalyzer.pml.operators.*
import sourcecode.Name

class DbusC2D2B8Platform(name: Symbol) extends Platform(name) {
  def this()(implicit implicitName: Name) = {
    this(Symbol(implicitName.value))
  }

  object rosace extends Composite {

    object cg0 extends Composite {

      object cl0 extends Composite {

        val C0: Initiator = Initiator()

        val C1: Initiator = Initiator()

        val bus: SimpleTransporter = SimpleTransporter()

        val input_port: SimpleTransporter = SimpleTransporter()

        val output_port: SimpleTransporter = SimpleTransporter()

        val C0_L1: Target = Target()

        val C1_L1: Target = Target()

        val l2: Target = Target()

        C0 link bus

        C1 link bus

        input_port link bus

        bus link output_port

        C0 link C0_L1

        C1 link C1_L1

        bus link l2
      }

      val input_port: SimpleTransporter = SimpleTransporter()

      val output_port: SimpleTransporter = SimpleTransporter()

      input_port link cl0.input_port

      cl0.output_port link output_port
    }

    object dg0 extends Composite {

      object cl0 extends Composite {

        val C0: Initiator = Initiator()

        val C1: Initiator = Initiator()

        val bus: SimpleTransporter = SimpleTransporter()

        val input_port: SimpleTransporter = SimpleTransporter()

        val output_port: SimpleTransporter = SimpleTransporter()

        val C0_SRAM: Target = Target()

        val C1_SRAM: Target = Target()

        C0 link bus

        C1 link bus

        input_port link bus

        bus link output_port

        C0 link C0_SRAM

        C1 link C1_SRAM
      }

      val input_port: SimpleTransporter = SimpleTransporter()

      val output_port: SimpleTransporter = SimpleTransporter()

      input_port link cl0.input_port

      cl0.output_port link output_port
    }

    object ddr extends Composite {

      val BK0: Target = Target()

      val BK1: Target = Target()

      val BK2: Target = Target()

      val BK3: Target = Target()

      val BK4: Target = Target()

      val BK5: Target = Target()

      val BK6: Target = Target()

      val BK7: Target = Target()

      val ddr_ctrl: SimpleTransporter = SimpleTransporter()

      val input_port: SimpleTransporter = SimpleTransporter()

      ddr_ctrl link BK0

      ddr_ctrl link BK1

      ddr_ctrl link BK2

      ddr_ctrl link BK3

      ddr_ctrl link BK4

      ddr_ctrl link BK5

      ddr_ctrl link BK6

      ddr_ctrl link BK7

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
