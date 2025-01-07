package onera.pmlanalyzer.pml.experiments.noc

import onera.pmlanalyzer.pml.model.hardware.*
import onera.pmlanalyzer.pml.operators.*
import sourcecode.Name

class NocC8S4G1B8Platform(name: Symbol) extends Platform(name) {
  def this()(implicit implicitName: Name) = {
    this(Symbol(implicitName.value))
  }

  object rosace extends Composite {

    object cg0 extends Composite {

      object cl0 extends Composite {

        val C0: Initiator = Initiator()

        val bus: SimpleTransporter = SimpleTransporter()

        val input_port: SimpleTransporter = SimpleTransporter()

        val output_port: SimpleTransporter = SimpleTransporter()

        val C0_L1: Target = Target()

        val l2: Target = Target()

        C0 link bus

        input_port link bus

        bus link output_port

        C0 link C0_L1

        bus link l2
      }

      object cl1 extends Composite {

        val C0: Initiator = Initiator()

        val bus: SimpleTransporter = SimpleTransporter()

        val input_port: SimpleTransporter = SimpleTransporter()

        val output_port: SimpleTransporter = SimpleTransporter()

        val C0_L1: Target = Target()

        val l2: Target = Target()

        C0 link bus

        input_port link bus

        bus link output_port

        C0 link C0_L1

        bus link l2
      }

      object cl2 extends Composite {

        val C0: Initiator = Initiator()

        val bus: SimpleTransporter = SimpleTransporter()

        val input_port: SimpleTransporter = SimpleTransporter()

        val output_port: SimpleTransporter = SimpleTransporter()

        val C0_L1: Target = Target()

        val l2: Target = Target()

        C0 link bus

        input_port link bus

        bus link output_port

        C0 link C0_L1

        bus link l2
      }

      object cl3 extends Composite {

        val C0: Initiator = Initiator()

        val bus: SimpleTransporter = SimpleTransporter()

        val input_port: SimpleTransporter = SimpleTransporter()

        val output_port: SimpleTransporter = SimpleTransporter()

        val C0_L1: Target = Target()

        val l2: Target = Target()

        C0 link bus

        input_port link bus

        bus link output_port

        C0 link C0_L1

        bus link l2
      }

      object cl4 extends Composite {

        val C0: Initiator = Initiator()

        val bus: SimpleTransporter = SimpleTransporter()

        val input_port: SimpleTransporter = SimpleTransporter()

        val output_port: SimpleTransporter = SimpleTransporter()

        val C0_L1: Target = Target()

        val l2: Target = Target()

        C0 link bus

        input_port link bus

        bus link output_port

        C0 link C0_L1

        bus link l2
      }

      object cl5 extends Composite {

        val C0: Initiator = Initiator()

        val bus: SimpleTransporter = SimpleTransporter()

        val input_port: SimpleTransporter = SimpleTransporter()

        val output_port: SimpleTransporter = SimpleTransporter()

        val C0_L1: Target = Target()

        val l2: Target = Target()

        C0 link bus

        input_port link bus

        bus link output_port

        C0 link C0_L1

        bus link l2
      }

      object cl6 extends Composite {

        val C0: Initiator = Initiator()

        val bus: SimpleTransporter = SimpleTransporter()

        val input_port: SimpleTransporter = SimpleTransporter()

        val output_port: SimpleTransporter = SimpleTransporter()

        val C0_L1: Target = Target()

        val l2: Target = Target()

        C0 link bus

        input_port link bus

        bus link output_port

        C0 link C0_L1

        bus link l2
      }

      object cl7 extends Composite {

        val C0: Initiator = Initiator()

        val bus: SimpleTransporter = SimpleTransporter()

        val input_port: SimpleTransporter = SimpleTransporter()

        val output_port: SimpleTransporter = SimpleTransporter()

        val C0_L1: Target = Target()

        val l2: Target = Target()

        C0 link bus

        input_port link bus

        bus link output_port

        C0 link C0_L1

        bus link l2
      }

      val L0_0: SimpleTransporter = SimpleTransporter()

      val L0_1: SimpleTransporter = SimpleTransporter()

      val L1_0: SimpleTransporter = SimpleTransporter()

      val input_port: SimpleTransporter = SimpleTransporter()

      val output_port: SimpleTransporter = SimpleTransporter()

      L0_0 link cl0.input_port

      cl0.output_port link L0_0

      L0_0 link cl1.input_port

      cl1.output_port link L0_0

      L0_0 link cl2.input_port

      cl2.output_port link L0_0

      L0_0 link cl3.input_port

      cl3.output_port link L0_0

      L0_1 link cl4.input_port

      cl4.output_port link L0_1

      L0_1 link cl5.input_port

      cl5.output_port link L0_1

      L0_1 link cl6.input_port

      cl6.output_port link L0_1

      L0_1 link cl7.input_port

      cl7.output_port link L0_1

      L1_0 link L0_0

      L0_0 link L1_0

      L1_0 link L0_1

      L0_1 link L1_0

      input_port link L1_0

      L1_0 link output_port
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

    cg0.output_port link pf_bus

    dma link pf_bus

    pf_bus link eth
  }

}
