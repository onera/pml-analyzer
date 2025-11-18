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

package examples.simpleKeystone.views.interference

import examples.simpleKeystone.pml.SimpleKeystonePlatform
import onera.pmlanalyzer.pml.operators.*
import onera.pmlanalyzer.views.interference.model.specification.InterferenceSpecification.AtomicTransactionId
import onera.pmlanalyzer.views.interference.model.specification.PhysicalTableBasedInterferenceSpecification
import onera.pmlanalyzer.views.interference.operators.*

/** The interference calculus assumptions for the hardware components of the
  * SimpleKeystone are gathered here. For instance to specify that two service l
  * and r interfere with each other if
  *
  *   - they are provided by the same owner except for
  *     [[SimpleKeystonePlatform.TeraNet.periph_bus]],
  *     [[SimpleKeystonePlatform.axi_bus]],
  *     [[SimpleKeystonePlatform.MemorySubsystem.msmc]]
  *   - they are provided by the
  *     [[SimpleKeystonePlatform.dma]] and
  *     [[SimpleKeystonePlatform.dma_reg]]
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
trait SimpleKeystonePhysicalTableBasedInterferenceSpecification
    extends PhysicalTableBasedInterferenceSpecification {
  self: SimpleKeystonePlatform =>

  for {
    l <- services
    r <- services
    if l != r
    if (l.hardwareOwnerIs(dma) && r.hardwareOwnerIs(dma_reg)) ||
      (l.hardwareOwner == r.hardwareOwner && !l.hardwareOwnerIs(
        TeraNet.periph_bus
      ) && !l.hardwareOwnerIs(axi_bus) && !l.hardwareOwnerIs(
        MemorySubsystem.msmc
      ))
  } yield {
    l interfereWith r
  }

  for {
    l <- atomicTransactions
    r <- atomicTransactions
    if l != r
    if l.usedInitiators == r.usedInitiators
  } yield {
    l exclusiveWith r
  }

}
