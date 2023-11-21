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

package onera.pmlanalyzer.views.interference.examples.simpleKeystone

import onera.pmlanalyzer.pml.examples.simpleKeystone.{SimpleKeystoneLibraryConfiguration, SimpleKeystonePlatform, SimpleKeystoneTransactionLibrary, SimpleSoftwareAllocation}
import onera.pmlanalyzer.pml.operators.*
import onera.pmlanalyzer.views.interference.model.specification.ApplicativeTableBasedInterferenceSpecification
import onera.pmlanalyzer.views.interference.operators.*

/**
  * The interference calculus assumptions for the SimpleKeystone's applications are gathered here.
  * For instance app22 and app3 cannot execute simultaneously so
  * {{{app22 exclusiveWith app3}}}
  * The app3 transfer transaction does not significantly impact the [[TeraNet]]
  * {{{app3_transfer notInterfereWith TeraNet.periph_bus.loads}}}
  * @see [[views.interference.operators.Exclusive.Ops]] for interfere operator definition
  */
trait SimpleKeystoneApplicativeTableBasedInterferenceSpecification extends ApplicativeTableBasedInterferenceSpecification {
  self: SimpleKeystonePlatform with SimpleKeystoneTransactionLibrary with SimpleKeystoneLibraryConfiguration with SimpleSoftwareAllocation   =>

  app22 exclusiveWith app3

  t11_app1_rd_interrupt1 notInterfereWith axi_bus.loads
  app3_transfer notInterfereWith TeraNet.periph_bus.loads
  app3_transfer notInterfereWith TeraNet.periph_bus.stores
  app3_transfer notInterfereWith MemorySubsystem.msmc.loads
}
