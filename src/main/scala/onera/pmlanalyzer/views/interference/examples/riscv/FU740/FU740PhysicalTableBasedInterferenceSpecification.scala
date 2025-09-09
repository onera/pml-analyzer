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
import onera.pmlanalyzer.pml.model.hardware.Target
import onera.pmlanalyzer.pml.operators.*
import onera.pmlanalyzer.views.interference.model.specification.InterferenceSpecification.PhysicalAtomicTransactionId
import onera.pmlanalyzer.views.interference.model.specification.PhysicalTableBasedInterferenceSpecification
import onera.pmlanalyzer.views.interference.operators.*

import scala.language.postfixOps

/**
  * The interference calculus assumptions for the hardware components of the FU740 are gathered here.
  */
trait FU740PhysicalTableBasedInterferenceSpecification
    extends PhysicalTableBasedInterferenceSpecification {
  self: FU740Platform with FU740LibraryConfiguration =>

  /* All services from a single component interfere with each other.
   */
  for {
    h <- self.hardware /* FIXME Should this be accessible without `self` ? */
  } {
    h hasInterferingServices
  }

  /* A single initiator cannot issue multiple transactions at once.
   * Expressed by tagging transactions from the same initiator as exclusive.
   */
  for {
    l <- transactions
    r <- transactions
    if l != r

    if l.pathInitiators == r.pathInitiators
  } {
    l exclusiveWith r
  }

  /* Transactions to the UART are slow.
   * Transactions to the UART should not cause interference on the cluster TileLink Switch.
   * Tagging all transactions from cores to UART targets as not interfering on the TileLink services.
   */
  for {
    t <- transactions
    c <- u74_cluster.cores
    u <- uart.hardware.collect({ case x: Target =>
      x
    }) /* FIXME Should has an extension to collect targets ? */
    if t.pathInitiatorIs(c)
    if t.pathTargetIs(u)

    s <- u74_cluster.tilelink_switch.services
  } {
    t notInterfereWith s
  }

}
