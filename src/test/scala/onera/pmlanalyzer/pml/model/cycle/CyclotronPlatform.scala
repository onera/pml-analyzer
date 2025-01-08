package onera.pmlanalyzer.pml.model.cycle

import onera.pmlanalyzer.pml.model.hardware.*
import onera.pmlanalyzer.pml.operators.*
import sourcecode.Name

class CyclotronPlatform(name: Symbol) extends Platform(name) {
  def this()(implicit implicitName: Name) = {
    this(Symbol(implicitName.value))
  }

  object group extends Composite {
    val L0: SimpleTransporter = SimpleTransporter()
    val L1: SimpleTransporter = SimpleTransporter()

    val input_port: SimpleTransporter = SimpleTransporter()
    val other_port: SimpleTransporter = SimpleTransporter()

    L1 link L0
    L0 link L1
    other_port link L0
  }

  val dma: Initiator = Initiator()
  val eth: Target = Target()
  dma link eth
}
