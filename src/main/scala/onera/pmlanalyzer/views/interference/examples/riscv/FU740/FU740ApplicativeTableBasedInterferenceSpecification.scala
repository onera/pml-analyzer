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

import onera.pmlanalyzer.pml.examples.riscv.FU740
import onera.pmlanalyzer.pml.examples.riscv.FU740.pml.{
  FU740LibraryConfiguration,
  FU740Platform,
  FU740SoftwareAllocation,
  FU740TransactionLibrary
}
import onera.pmlanalyzer.pml.operators.*
import onera.pmlanalyzer.views.interference.model.specification.ApplicativeTableBasedInterferenceSpecification
import onera.pmlanalyzer.views.interference.operators.*

/**
  * The interference calculus assumptions for the SimpleZynqUltraScale's applications are gathered here.
  * For instance app22 and app3 cannot execute simultaneously so
  * {{{app22 exclusiveWith app3}}}
  * The app3 transfer transaction does not significantly impact the [[pml.examples.simpleZynqUltraScale.SimpleZynqUltraScalePlatform.TeraNet]]
  * {{{app3_transfer notInterfereWith TeraNet.periph_bus.loads}}}
  * @see [[views.interference.operators.Exclusive.Ops]] for interfere operator definition
  */
trait FU740ApplicativeTableBasedInterferenceSpecification extends ApplicativeTableBasedInterferenceSpecification {
  self: FU740Platform with FU740TransactionLibrary with FU740LibraryConfiguration with FU740SoftwareAllocation   =>

  // Sequential execution
//  app31 exclusiveWith app32

}
