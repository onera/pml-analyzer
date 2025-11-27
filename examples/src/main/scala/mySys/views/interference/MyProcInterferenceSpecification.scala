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

package mySys.views.interference

import mySys.pml.MyProcPlatform
import onera.pmlanalyzer.*
import onera.pmlanalyzer.pml.model.hardware.Initiator
import onera.pmlanalyzer.pml.model.service.Service
import onera.pmlanalyzer.views.interference.model.specification.PhysicalTableBasedInterferenceSpecification

/** The interference calculus assumptions for the hardware components of the
  * MySys are gathered here. For instance to specify that two service l and r
  * interfere with each other if
  *
  *   - they are provided by the same owner except for
  * [[pml.examples.mySys.MyProcPlatform.TeraNet.periph_bus]],
  * [[pml.examples.mySys.MyProcPlatform.axi_bus]],
  * [[pml.examples.mySys.MyProcPlatform.MemorySubsystem.msmc]]
  *   - they are provided by the [[pml.examples.mySys.MyProcPlatform.dma]] and
  *     [[pml.examples.mySys.MyProcPlatform.dma_reg]]
  *     {{{
  *  for {
  *   l <- services
  *   r <- services
  *   if l != r
  *   if (l.hardwareOwnerIs(dma) && r.hardwareOwnerIs(dma_reg)) ||
  *    (l.hardwareOwner == r.hardwareOwner && !l.hardwareOwnerIs(TeraNet.periph_bus)
  *     && !l.hardwareOwnerIs(axi_bus) && !l.hardwareOwnerIs(MemorySubsystem.msmc))
  *  } yield {
  *   l interfereWith r
  *  }
  *     }}}
  */
trait MyProcInterferenceSpecification
    extends PhysicalTableBasedInterferenceSpecification {
  self: MyProcPlatform =>

  // Encoding of Rule 1 and Rule 2
  for {
    l <- services
    r <- services
    if l != r
    if
    // (l.hardwareOwnerIs(dma) && r.hardwareOwnerIs(dma_reg)) ||
    (l.hardwareOwner == r.hardwareOwner
      &&
      !l.hardwareOwnerIs(
        TeraNet.periph_bus
      ) && !l.hardwareOwnerIs(axi_bus) && !l.hardwareOwnerIs(
        MemorySubsystem.msmc
      ))
  } yield {
    l interfereWith r
  }

  dma interfereWith dma_reg
  // TeraNet.periph_bus.hasNonInterferingServices axi_bus
  // TeraNet.periph_bus notInterfereWith MemorySubsystem.msmc
  // MemorySubsystem.msmc.hasNonInterferingServices
  // axi_bus.hasNonInterferingServices

  // All transactions issued from the same initiator are exclusive
  for {
    l <- atomicTransactions
    r <- atomicTransactions
    if l != r
    if l.usedInitiators == r.usedInitiators
  } yield {
    l exclusiveWith r
  }

}
