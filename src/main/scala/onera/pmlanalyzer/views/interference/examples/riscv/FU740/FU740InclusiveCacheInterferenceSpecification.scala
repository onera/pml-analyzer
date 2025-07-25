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

trait FU740InclusiveCacheInterferenceSpecification
    extends PhysicalTableBasedInterferenceSpecification {
  self: FU740Platform with FU740LibraryConfiguration =>

  for {
    (core, partition) <- u74_cluster.coreToL2Partition
    core_complex <- u74_cluster.U74.filter(_.core == core)

    s <- partition.services
    t <- core_complex.dl1_cache.services ++ core_complex.il1_cache.services
  } {
    s interfereWith t
  }

}
