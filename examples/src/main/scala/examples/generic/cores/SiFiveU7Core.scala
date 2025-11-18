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

package examples.generic.cores

import onera.pmlanalyzer.pml.model.hardware.*
import onera.pmlanalyzer.pml.model.relations.*
import onera.pmlanalyzer.pml.model.utils.*
import onera.pmlanalyzer.pml.operators.*
import sourcecode.Name

/** Simple model of the SiFive U7 Core. */
class SiFiveU7Core(
    name: String,
    coreInfo: ReflexiveInfo,
    coreContext: Context
) extends Composite(Symbol(name), coreInfo, coreContext) {

  def this(_name: String, dummy: Int = 0)(using
      givenInfo: ReflexiveInfo,
      givenContext: Context
  ) = {
    this(_name, givenInfo, givenContext)
  }

  def this()(using
      givenName: Name,
      givenInfo: ReflexiveInfo,
      givenContext: Context
  ) = {
    this(givenName.value, givenInfo, givenContext)
  }

  // Initiator modelling a SiFive U7 Core (includes branch predictor)
  val core: Initiator = Initiator()

  // Target modelling the L1 caches of the core
  val il1_cache: Target = Target()
  val dl1_cache: Target = Target()

  // Transporter modelling the L1 cache memory controllers
  val il1_mem_ctrl: SimpleTransporter = SimpleTransporter()
  val dl1_mem_ctrl: SimpleTransporter = SimpleTransporter()

  // Target modelling the L1 and L2 TLBs of the core
  val dl1_tlb: Target = Target()
  val il1_tlb: Target = Target()
  val l2_tlb: Target = Target()

  // Transporter modelling the L1 and L2 TLB controllers of the core
  val dl1_tlb_ctrl: SimpleTransporter = SimpleTransporter()
  val il1_tlb_ctrl: SimpleTransporter = SimpleTransporter()
  val l2_tlb_ctrl: SimpleTransporter = SimpleTransporter()

  // Internal connections

  // SiFive core access to its private L1 and L2 cache tlb
  core link dl1_tlb_ctrl
  core link il1_tlb_ctrl
  dl1_tlb_ctrl link dl1_tlb
  il1_tlb_ctrl link il1_tlb
  dl1_tlb_ctrl link l2_tlb_ctrl
  il1_tlb_ctrl link l2_tlb_ctrl
  l2_tlb_ctrl link l2_tlb

  // The Last Level TLB is connected to the L1D$
  l2_tlb_ctrl link dl1_mem_ctrl

  // SiFive core access to its private L1 and shared L2 cache
  core link dl1_mem_ctrl
  core link il1_mem_ctrl
  il1_mem_ctrl link il1_cache
  dl1_mem_ctrl link dl1_cache
}
