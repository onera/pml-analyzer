/*******************************************************************************
 * Copyright (c)  2025. ONERA
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

package onera.pmlanalyzer.pml.examples.riscv.FU740.components

import onera.pmlanalyzer.pml.examples.components.cores.{SiFiveS7Core, SiFiveU7Core}
import onera.pmlanalyzer.pml.model.hardware.*
import onera.pmlanalyzer.pml.model.relations.*
import onera.pmlanalyzer.pml.model.utils.*
import onera.pmlanalyzer.pml.operators.*
import sourcecode.Name

/** Simple model of the SiFive U74 Core Complex. */
class U74CoreComplex(
    name: String,
    u74_nb: Int,
    channels_nb: Int,
    bank_nb: Int,
    partitionLlc: Boolean,
    complexInfo: ReflexiveInfo,
    complexContext: Context
) extends Composite(Symbol(name), complexInfo, complexContext) {

  def this(
      name: String,
      u74_nb: Int,
      channels_nb: Int,
      bank_nb: Int,
      partitionLlc: Boolean,
      dummy: Int = 0
  )(using
      givenInfo: ReflexiveInfo,
      givenContext: Context
  ) = {
    this(
      name,
      u74_nb,
      channels_nb,
      bank_nb,
      partitionLlc,
      givenInfo,
      givenContext
    )
  }

  def this(u74_nb: Int, channels_nb: Int, bank_nb: Int, partitionLlc: Boolean)(
      using
      givenName: Name,
      givenInfo: ReflexiveInfo,
      givenContext: Context
  ) = {
    this(
      givenName.value,
      u74_nb,
      channels_nb,
      bank_nb,
      partitionLlc,
      givenInfo,
      givenContext
    )
  }

  /** Composite representing the Direct Memory Access (DMA)
   *
   * @group composite_def
   */
  class Direct_memory_access(
      channels_nb: Int,
      name: String,
      dmaInfo: ReflexiveInfo,
      dmaContext: Context
  ) extends Composite(
        Symbol(name),
        dmaInfo: ReflexiveInfo,
        dmaContext: Context
      ) {

    /**
     * Enable to provide the name implicitly
     *
     * @param implicitName the name of the object/class inheriting from this class
     *                     will be the name of composite
     */
    def this(channels_nb: Int)(using
        implicitName: Name,
        givenInfo: ReflexiveInfo,
        givenContext: Context
    ) = {
      this(channels_nb, implicitName.value, givenInfo, givenContext)
    }

    /** Initiator modelling the DMAs
     *
     * @group initiator */
    val channel: IndexedSeq[Initiator] =
      (0 until channels_nb).map(i => Initiator(s"Channel$i"))

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
    for (i <- 0 until channels_nb) {
      channel(i) link arbiter
    }

    // Connections between arbiters and ports and registers
    arbiter link ctrl_reg
    arbiter link master_port
    slave_port link arbiter

  }

  // Composites modelling the SiFive S7 core 0, the SiFive U7 cores 1-4 and the DMA
  val C0 = new SiFiveS7Core()
  val S74: IndexedSeq[SiFiveS7Core] = IndexedSeq(C0)

  val U74: IndexedSeq[SiFiveU7Core] =
    (0 until u74_nb).map(i => SiFiveU7Core(s"C${i + 1}"))
  val dma = new Direct_memory_access(channels_nb)

  val cores: Seq[Initiator] = Seq(C0.core) ++ (for (c <- U74) yield {
    c.core
  })

  // Transporter modelling the interconnection switches
  val TileLinkSwitch: SimpleTransporter = SimpleTransporter()
  val Peripheral_TL_Switch_0: SimpleTransporter = SimpleTransporter()
  val Peripheral_TL_Switch_1: SimpleTransporter = SimpleTransporter()

  // Transporter modelling the two possible paths to L2$ Controller (slow for the LIM and fast for the cacheable)
  val slow_path: SimpleTransporter = SimpleTransporter()
  val fast_path: SimpleTransporter = SimpleTransporter()

  // Transporter modelling the L2 Snoop Controller
  val L2_Ctrl: SimpleTransporter = SimpleTransporter()

  // Transport and Targets modelling the unified L2 memory of the core.
  // Note that the memory can be of type LIM or Cache.
  // Here we differentiate between both for implementation easiness of them. However, this is not exact.
  val L2_memory_port: SimpleTransporter = SimpleTransporter()
  val L2_Bank: IndexedSeq[SimpleTransporter] =
    (0 until bank_nb).map(i => SimpleTransporter(s"Bank$i"))
  val L2_cache_prt: IndexedSeq[Target] =
    if (partitionLlc) {
      for { i <- 0 until cores.length } yield {
        Target(s"L2CachePrt$i")
      }
    } else {
      IndexedSeq(Target("L2Cache"))
    }

  val CoreToL2Partition = cores.zipAll(L2_cache_prt, null, L2_cache_prt.last)

  val L2_LIM: Target = Target()

  // Target modelling the directory for coherency manager
  val directory: Target = Target()

  // Target modelling the partitioned unified L2 cache of the core
  val MSHR: Target = Target()
  val WB_Buffer: Target = Target()

  // Transporter modelling the Memory Bus (path to DDR SDRAM)
  val MBus: SimpleTransporter = SimpleTransporter()

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
  cores foreach (_ link TileLinkSwitch)

  dma.master_port link TileLinkSwitch

  TileLinkSwitch link dma.slave_port
  TileLinkSwitch link slow_path
  TileLinkSwitch link fast_path
  TileLinkSwitch link Peripheral_TL_Switch_0
  TileLinkSwitch link Peripheral_TL_Switch_1
  slow_path link L2_Ctrl
  fast_path link L2_Ctrl

// Controller to different buffers and memory
  L2_Ctrl link directory
  L2_Ctrl link L2_memory_port

// L2 cache memory port to Bank connections
  for (j <- 0 until bank_nb) {
    L2_memory_port link L2_Bank(j)
  }
// Banks to LIM connection
  for (j <- 0 until bank_nb) {
    L2_Bank(j) link L2_LIM
  }
// Banks to cache partitions connections
  for {
    partition <- L2_cache_prt
    bank <- L2_Bank
  } {
    bank link partition
  }

  L2_Ctrl link MSHR
  L2_Ctrl link WB_Buffer

// Output
  L2_Ctrl link MBus
}
