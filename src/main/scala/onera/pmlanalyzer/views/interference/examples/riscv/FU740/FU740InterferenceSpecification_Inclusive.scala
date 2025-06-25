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

package onera.pmlanalyzer.views.interference.examples.riscv.FU740

import onera.pmlanalyzer.pml.examples.riscv.FU740.pml.*
import onera.pmlanalyzer.pml.operators.*
import onera.pmlanalyzer.views.interference.model.specification.PhysicalTableBasedInterferenceSpecification
import onera.pmlanalyzer.views.interference.operators.*

trait FU740InterferenceSpecification_Inclusive extends PhysicalTableBasedInterferenceSpecification {
  self: FU740Platform with FU740LibraryConfiguration =>

//  for {
//    (core, partition) <- Cluster_U74_0.CoreToL2Partition
//    core_complex <- Cluster_U74_0.U74.filter(_.core == core)
//
//    s <- partition.services
//    t <- core_complex.L1D_cache.services ++ core_complex.L1I_cache.services
//  } {
//    s interfereWith t
//    println(s"${s} interferes with ${t}")
//  }

//  for {
//    p <- Cluster_U74_0.L2_cache_prt
//    c <- Cluster_U74_0.U74
//
//    s <- p.services
//    t <- c.L1D_cache.services
//  } {
//    s interfereWith t
//    println(s"${s} interferes with ${t}")
//  }

  for {
    c <- u74_cluster.U74
    s <- u74_cluster.C0.dtim.services
    t <- c.dl1_cache.services
  } {
    s interfereWith t
  }


}
