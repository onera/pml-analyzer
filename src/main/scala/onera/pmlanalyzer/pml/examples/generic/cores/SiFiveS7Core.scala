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

package onera.pmlanalyzer.pml.examples.generic.cores

import onera.pmlanalyzer.pml.model.hardware.*
import onera.pmlanalyzer.pml.model.relations.*
import onera.pmlanalyzer.pml.model.utils.*
import onera.pmlanalyzer.pml.operators.*
import sourcecode.Name

/** Simple model of the SiFive S7 Core. */
class SiFiveS7Core(name: String, coreInfo: ReflexiveInfo, coreContext: Context)
    extends Composite(Symbol(name), coreInfo, coreContext) {

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

  // Target modelling the L1 cache and memories of the core
  val il1_cache: Target = Target()
  val dtim: Target = Target()

  // Transporter modelling the L1 cache memory controllers
  val il1_mem_ctrl: SimpleTransporter = SimpleTransporter()

  // Internal connections
  core link dtim
  core link il1_mem_ctrl
  il1_mem_ctrl link il1_cache

}
