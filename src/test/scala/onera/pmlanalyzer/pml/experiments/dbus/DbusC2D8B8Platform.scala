package onera.pmlanalyzer.pml.experiments.dbus

import onera.pmlanalyzer.pml.model.hardware.*
import onera.pmlanalyzer.pml.operators.*
import sourcecode.Name

class DbusC2D8B8Platform(name: Symbol) extends Platform(name) {
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

        bus link output_port

        C0 link C0_L1

        C1 link C1_L1

        bus link l2
      }

      val input_port: SimpleTransporter = SimpleTransporter()

      val output_port: SimpleTransporter = SimpleTransporter()

      cl0.output_port link output_port
    }

    object dg0 extends Composite {

      object cl0 extends Composite {

        val C0: Initiator = Initiator()

        val C1: Initiator = Initiator()

        val C2: Initiator = Initiator()

        val C3: Initiator = Initiator()

        val C4: Initiator = Initiator()

        val C5: Initiator = Initiator()

        val C6: Initiator = Initiator()

        val C7: Initiator = Initiator()

        val bus: SimpleTransporter = SimpleTransporter()

        val input_port: SimpleTransporter = SimpleTransporter()

        val output_port: SimpleTransporter = SimpleTransporter()

        val C0_SRAM: Target = Target()

        val C1_SRAM: Target = Target()

        val C2_SRAM: Target = Target()

        val C3_SRAM: Target = Target()

        val C4_SRAM: Target = Target()

        val C5_SRAM: Target = Target()

        val C6_SRAM: Target = Target()

        val C7_SRAM: Target = Target()

        C0 link bus

        C1 link bus

        C2 link bus

        C3 link bus

        C4 link bus

        C5 link bus

        C6 link bus

        C7 link bus

        bus link output_port

        C0 link C0_SRAM

        C1 link C1_SRAM

        C2 link C2_SRAM

        C3 link C3_SRAM

        C4 link C4_SRAM

        C5 link C5_SRAM

        C6 link C6_SRAM

        C7 link C7_SRAM
      }

      val input_port: SimpleTransporter = SimpleTransporter()

      val output_port: SimpleTransporter = SimpleTransporter()

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
