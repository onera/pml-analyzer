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
  for (i <- Initiator.all.filterNot(Cluster_U74_0.dma.channel.contains(_))) {
    i targeting Target.all blockedBy Cluster_U74_0.dma.master_port
  }

  /* Accesses from the DMA go through the master port.
   * Accesses from the DMA cannot use the Fast Path.
   */
  for {
    channel <- Cluster_U74_0.dma.channel
  } {
    channel targeting Target.all blockedBy Cluster_U74_0.dma.slave_port
    channel targeting Target.all blockedBy Cluster_U74_0.fast_path
  }

  /* Accesses to local caches do not leave the U7 complex. */
  for {
    c <- Cluster_U74_0.U74
    cache <- Seq(c.L1D_cache, c.L1I_cache) //, c.L2_tlb, c.L1D_tlb, c.L1I_tlb)
  } {
    c.core targeting cache blockedBy Cluster_U74_0.TileLinkSwitch
  }

  /* Routing restrictions regarding fast and slow paths to L2 memory. */
  for {
    c <- Cluster_U74_0.cores
  } {
    c targeting Cluster_U74_0.L2_cache_prt blockedBy Cluster_U74_0.slow_path
    c targeting Cluster_U74_0.L2_LIM blockedBy Cluster_U74_0.fast_path
    c targeting DDR.banks blockedBy Cluster_U74_0.slow_path
  }

  /* Force direct path towards out of core memories
   * FIXME Unsound constraint if other initiators may access the core-local memories.
   * The constraints only works under the assumption that core-local memories
   * cannot be a source of interference. Thus we can ignore the footprint of
   * the local core when it leaves its locality (to the L2 or memory).
   */
  for {
    core <- Cluster_U74_0.U74.map(_.core)
  } {
    core targeting Cluster_U74_0.L2_cache_prt useLink core to Cluster_U74_0.TileLinkSwitch
    core targeting DDR.banks useLink core to Cluster_U74_0.TileLinkSwitch
  }
}
