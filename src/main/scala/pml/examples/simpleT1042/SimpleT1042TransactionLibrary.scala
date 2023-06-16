package pml.examples.simpleT1042

import pml.model.configuration.TransactionLibrary
import pml.operators._

import scala.language.postfixOps

trait SimpleT1042TransactionLibrary extends TransactionLibrary{
  self: SimpleT1042Platform with SimpleSoftwareAllocation =>

  /** -----------------------------------------------------------
    * Target usage
    * ----------------------------------------------------------- */

  val app4_wr_input_d : Transaction = Transaction(app4 write input_d)
  val app21_rd_input_d : Transaction = Transaction(app21 read input_d)
  val app21_wr_d1 : Transaction = Transaction(app21 write d1)
  val app1_rd_interrupt1 : Transaction = Transaction(app1 read interrupt1)
  val app1_rd_d1 : Transaction = Transaction(app1 read d1)
  val app1_wr_d2 : Transaction = Transaction(app1 write d2)
  val app1_rd_L1: Transaction = Transaction( app1 read C1.L1)
  val app1_wr_L1: Transaction = Transaction( app1 write C1.L1)
  val app1_rd_wr_L1 : Scenario = Scenario(app1_rd_L1, app1_wr_L1)
  val app22_rd_d2 : Transaction = Transaction(app22 read d2)
  val app22_wr_output_d : Transaction = Transaction(app22 write output_d)
  val app22_st_dma_reg : Transaction = Transaction(app22 write dma_reg)


/** -----------------------------------------------------------
  * DMA copies
  * ----------------------------------------------------------- */
//  val app3_rd_output_d : Transaction = Transaction(app3 read output_d)
//  val app3_wr_output_pcie_frame : Transaction = Transaction(app3 write output_pcie_frame)
  val app3_transfer: Scenario = Scenario(app3 read output_d, app3 write output_pcie_frame)
}
