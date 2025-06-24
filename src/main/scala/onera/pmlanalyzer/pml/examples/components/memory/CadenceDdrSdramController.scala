package onera.pmlanalyzer.pml.examples.components.memory

import onera.pmlanalyzer.pml.model.hardware.*
import onera.pmlanalyzer.pml.model.relations.*
import onera.pmlanalyzer.pml.model.utils.*
import onera.pmlanalyzer.pml.operators.*
import sourcecode.Name

class CadenceDdrSdramController(
    input_bus_nb: Int,
    name: Symbol,
    ctrlInfo: ReflexiveInfo,
    ctrlContext: Context
) extends Composite(name, ctrlInfo, ctrlContext) {

  def this(input_bus_nb: Int)(using
      givenName: Name,
      givenInfo: ReflexiveInfo,
      givenContext: Context
  ) = {
    this(input_bus_nb, Symbol(givenName.value), givenInfo, givenContext)
  }

  // Transporter modelling the single input port
  val TileLink: IndexedSeq[SimpleTransporter] =
    (0 until input_bus_nb).map(i => SimpleTransporter(s"S$i"))

  // Transporter modelling the DDR controller
  val arbiter: SimpleTransporter = SimpleTransporter()
  val scheduler: SimpleTransporter = SimpleTransporter()
  val CMD_queue: SimpleTransporter = SimpleTransporter()

  // Intern connections
  for (i <- 0 until input_bus_nb) {
    TileLink(i) link arbiter
  }

  // Memory controller sub-units
  arbiter link CMD_queue
  CMD_queue link scheduler

}
