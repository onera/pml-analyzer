package onera.pmlanalyzer.pml.examples.components.io

import onera.pmlanalyzer.pml.model.hardware.*
import onera.pmlanalyzer.pml.model.relations.*
import onera.pmlanalyzer.pml.model.utils.*
import onera.pmlanalyzer.pml.operators.*
import sourcecode.Name

/** Universal Asynchronous Receiver Transmitter (UART)
 *
 * @group composite_def
 */
class Uart(name: String, uartInfo: ReflexiveInfo, uartContext: Context)
    extends Composite(Symbol(name), uartInfo, uartContext) {

  /**
   * Enable to provide the name implicitly
   *
   * @param givenName the name of the object/class inheriting from this class
   *                     will be the name of composite
   */
  def this()(using
      givenName: Name,
      givenInfo: ReflexiveInfo,
      givenContext: Context
  ) = {
    this(givenName.value, givenInfo, givenContext)
  }

  /** Transporter modelling the UART controller
   *
   * @group transporter */
  val ctrl: SimpleTransporter = SimpleTransporter()

  /** Targets modelling the FIFOs for TX and RX
   *
   * @group transporter */
  val FIFO_tx: Target = Target()
  val FIFO_rx: Target = Target()
  val ctrl_interrupt_regs: Target = Target()

  /** Transporter modelling the slave port
   *
   * @group transporter */
  val slave_port: SimpleTransporter = SimpleTransporter()

  // SPI connections
  slave_port link ctrl
  ctrl link FIFO_tx
  ctrl link FIFO_rx
  ctrl link ctrl_interrupt_regs
}
