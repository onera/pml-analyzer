package onera.pmlanalyzer.pml.examples.generic.memory

import onera.pmlanalyzer.pml.model.hardware.*
import onera.pmlanalyzer.pml.model.relations.*
import onera.pmlanalyzer.pml.model.utils.*
import onera.pmlanalyzer.pml.operators.*
import sourcecode.Name

class CadenceDdrSdramController(
    inputPortCnt: Int,
    name: Symbol,
    ctrlInfo: ReflexiveInfo,
    ctrlContext: Context
) extends Composite(name, ctrlInfo, ctrlContext) {

  def this(_inputPortCnt: Int)(using
                               givenName: Name,
                               givenInfo: ReflexiveInfo,
                               givenContext: Context
  ) = {
    this(_inputPortCnt, Symbol(givenName.value), givenInfo, givenContext)
  }

  // Transporter modelling the configurable input ports
  val input_ports: IndexedSeq[SimpleTransporter] =
    (0 until inputPortCnt).map(i => SimpleTransporter(s"S$i"))

  // Transporter modelling the DDR controller
  val arbiter: SimpleTransporter = SimpleTransporter()
  val scheduler: SimpleTransporter = SimpleTransporter()
  val cmd_queue: SimpleTransporter = SimpleTransporter()

  // Internal connections from input ports
  input_ports.foreach(_ link arbiter)

  // Memory controller sub-units
  arbiter link cmd_queue
  cmd_queue link scheduler
}
