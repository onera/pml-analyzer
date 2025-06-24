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
  val tx_fifo: Target = Target()
  val rx_fifo: Target = Target()

  /** Transporter modelling the slave port
   *
   * @group transporter */
  val slave_port: SimpleTransporter = SimpleTransporter()

  /** Controller interrupt registers
   *
   * @group target */
  val ctrl_interrupt_regs: Target = Target()

  // SPI connections
  slave_port link ctrl
  ctrl link tx_fifo
  ctrl link rx_fifo
  ctrl link ctrl_interrupt_regs
}
