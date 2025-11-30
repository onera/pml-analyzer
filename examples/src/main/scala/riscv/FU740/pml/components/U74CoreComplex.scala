/*******************************************************************************
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
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA
 ******************************************************************************/

package riscv.FU740.pml.components

import generic.cores.{SiFiveS7Core, SiFiveU7Core}
import onera.pmlanalyzer.*
import sourcecode.Name

/** Simple model of the SiFive U74 Core Complex. */
class U74CoreComplex(
    name: String,
    u74CoreCnt: Int,
    dmaChannelCnt: Int,
    l2BankCnt: Int,
    l2Partitioned: Boolean,
    complexInfo: ReflexiveInfo,
    complexContext: Context
) extends Composite(Symbol(name), complexInfo, complexContext) {

  def this(
      _name: String,
      _u74CoreCnt: Int,
      _dmaChannelCnt: Int,
      _l2BankCnt: Int,
      _l2Partitioned: Boolean,
      dummy: Int = 0
  )(using
      givenInfo: ReflexiveInfo,
      givenContext: Context
  ) = {
    this(
      _name,
      _u74CoreCnt,
      _dmaChannelCnt,
      _l2BankCnt,
      _l2Partitioned,
      givenInfo,
      givenContext
    )
  }

  def this(
      _u74CoreNb: Int,
      _dmaChannelCnt: Int,
      _l2BankCnt: Int,
      _l2Partitioned: Boolean
  )(using
      givenName: Name,
      givenInfo: ReflexiveInfo,
      givenContext: Context
  ) = {
    this(
      givenName.value,
      _u74CoreNb,
      _dmaChannelCnt,
      _l2BankCnt,
      _l2Partitioned,
      givenInfo,
      givenContext
    )
  }

  /** Composite representing the Direct Memory Access (DMA)
   *
   * @group composite_def
   */
  final class DirectMemoryAccess(
      channelCnt: Int,
      dmaName: String,
      dmaInfo: ReflexiveInfo,
      dmaContext: Context
  ) extends Composite(
        Symbol(dmaName),
        dmaInfo: ReflexiveInfo,
        dmaContext: Context
      ) {

    /**
     * Enable to provide the name implicitly
     *
     * @param implicitName the name of the object/class inheriting from this class
     *                     will be the name of composite
     */
    def this(_channelCnt: Int)(using
        implicitName: Name,
        givenInfo: ReflexiveInfo,
        givenContext: Context
    ) = {
      this(_channelCnt, implicitName.value, givenInfo, givenContext)
    }

    /** Initiator modelling the DMAs
     *
     * @group initiator */
    val channel: Seq[Initiator] =
      for (i <- 0 until channelCnt)
        yield Initiator(s"Channel$i")

    /** Transporter modelling the master and slave ports that go to the Tilelink Switch
     *
     * @group transporter */
    val master_port: SimpleTransporter = SimpleTransporter()
    val slave_port: SimpleTransporter = SimpleTransporter()

    /** Transporter modelling theDMA arbiter. This is not officially documented but I judge that some sort of
     * requests controller is required
     *
     * @group transporter */
    val arbiter: SimpleTransporter = SimpleTransporter()

    /** Target modelling the Control Register
     *
     * @group target */
    val ctrl_reg: Target = Target()

    // DMA channels to arbiter connections
    for (i <- 0 until channelCnt) {
      channel(i) link arbiter
    }

    // Connections between arbiters and ports and registers
    arbiter link ctrl_reg
    arbiter link master_port
    slave_port link arbiter

  }

  // Composites modelling the SiFive S7 core 0, the SiFive U7 cores 1-4 and the DMA
  val C0 = new SiFiveS7Core()
  val S74: Seq[SiFiveS7Core] = Seq(C0)

  val U74: Seq[SiFiveU7Core] =
    for (i <- 1 to u74CoreCnt) yield SiFiveU7Core(s"C$i")

  val dma = new DirectMemoryAccess(dmaChannelCnt)

  // Gather all S7 and U7 cores
  val cores: Seq[Initiator] = C0.core +: U74.map(_.core)

  // Transporter modelling the interconnection switches
  val tilelink_switch: SimpleTransporter = SimpleTransporter()
  val peripheral_tl_switch_0: SimpleTransporter = SimpleTransporter()
  val peripheral_tl_switch_1: SimpleTransporter = SimpleTransporter()

  // Transporter modelling the two possible paths to L2$ Controller (slow for the LIM and fast for the cacheable)
  val slow_path: SimpleTransporter = SimpleTransporter()
  val fast_path: SimpleTransporter = SimpleTransporter()

  // Transporter modelling the L2 Snoop Controller
  val l2_ctrl: SimpleTransporter = SimpleTransporter()

  // Transport and Targets modelling the unified L2 memory of the core.
  // Note that the memory can be of type LIM or Cache.
  // Here we differentiate between both for implementation easiness of them. However, this is not exact.
  val l2_memory_port: SimpleTransporter = SimpleTransporter()
  val l2_banks: Seq[SimpleTransporter] =
    for (i <- 0 until l2BankCnt)
      yield SimpleTransporter(s"Bank$i")

  val l2_cache_prts: Seq[Target] =
    if (l2Partitioned) {
      for (i <- cores.indices)
        yield Target(s"L2CachePrt$i")
    } else {
      Seq(Target("L2Cache"))
    }

  val coreToL2Partition: Seq[(Initiator, Target)] =
    for (i <- cores.indices)
      yield (
        cores(i),
        if (i < l2_cache_prts.size) l2_cache_prts(i) else l2_cache_prts.last
      )

  val l2_lim: Target = Target()

  // Target modelling the directory for coherency manager
  val directory: Target = Target()

  // Target modelling the partitioned unified L2 cache of the core
  val mshr: Target = Target()
  val wb_buffer: Target = Target()

  // Transporter modelling the Memory Bus (path to DDR SDRAM)
  val mem_bus: SimpleTransporter = SimpleTransporter()

  // SiFive cores access to the unified L2 memory controller and resources, directly if it goes to the LIM or
  // via de cache controllers in the rest of the case
// FIXME Restore L1 controllers to TileLinkSwitch connection once routing constraint fixed (see "Force direct path towards out of core memories")
//  C0.core link TileLinkSwitch
//  C0.L1I_mem_ctrl link TileLinkSwitch
//  for (i <- 0 until u74_nb) {
//    U74(i).core link TileLinkSwitch
//    U74(i).L1D_mem_ctrl link TileLinkSwitch
//    U74(i).L1I_mem_ctrl link TileLinkSwitch
//  }
  for (c <- cores) {
    c link tilelink_switch
  }

  dma.master_port link tilelink_switch

  tilelink_switch link dma.slave_port
  tilelink_switch link slow_path
  tilelink_switch link fast_path
  tilelink_switch link peripheral_tl_switch_0
  tilelink_switch link peripheral_tl_switch_1
  slow_path link l2_ctrl
  fast_path link l2_ctrl

// Controller to different buffers and memory
  l2_ctrl link directory
  l2_ctrl link l2_memory_port

  for (bank <- l2_banks) {
    // L2 cache memory port to Bank connections
    l2_memory_port link bank
    // Banks to LIM connection
    bank link l2_lim
  }

// Banks to cache partitions connections
  for {
    partition <- l2_cache_prts
    bank <- l2_banks
  } {
    bank link partition
  }

  l2_ctrl link mshr
  l2_ctrl link wb_buffer

  // Output
  l2_ctrl link mem_bus
}
