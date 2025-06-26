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

package onera.pmlanalyzer.pml.examples.riscv.FU740.pml

import onera.pmlanalyzer.pml.model.hardware.{Initiator, Target}
import onera.pmlanalyzer.pml.operators.*

/**
  * Routing constraints considered for the FU740 SoC
  */
trait RoutingConfiguration {
  self: FU740Platform =>

  /* Accesses to the DMA go through the slave port. */
  for (i <- Initiator.all -- u74_cluster.dma.channel) {
    i targeting Target.all blockedBy u74_cluster.dma.master_port
  }

  /* Accesses from the DMA go through the master port.
   * Accesses from the DMA cannot use the Fast Path.
   */
  for {
    channel <- u74_cluster.dma.channel
  } {
    channel targeting Target.all blockedBy u74_cluster.dma.slave_port
    channel targeting Target.all blockedBy u74_cluster.fast_path
  }

  /* Accesses to local caches do not leave the U7 complex. */
  for {
    c <- u74_cluster.U74
    cache <- Seq(c.dl1_cache, c.il1_cache) // , c.L2_tlb, c.L1D_tlb, c.L1I_tlb)
  } {
    c.core targeting cache blockedBy u74_cluster.tilelink_switch
  }

  /* Routing restrictions regarding fast and slow paths to L2 memory. */
  for {
    c <- u74_cluster.cores
  } {
    c targeting u74_cluster.l2_cache_prts blockedBy u74_cluster.slow_path
    c targeting u74_cluster.l2_lim blockedBy u74_cluster.fast_path
    c targeting ddr.banks blockedBy u74_cluster.slow_path
  }

  /* Force direct path towards out of core memories
   * FIXME Unsound constraint if other initiators may access the core-local memories.
   * The constraints only works under the assumption that core-local memories
   * cannot be a source of interference. Thus we can ignore the footprint of
   * the local core when it leaves its locality (to the L2 or memory).
   */
  for {
    u7 <- u74_cluster.U74
    core = u7.core
  } {
    core targeting u74_cluster.l2_cache_prts useLink core to u74_cluster.tilelink_switch
    core targeting ddr.banks useLink core to u74_cluster.tilelink_switch
  }
}
