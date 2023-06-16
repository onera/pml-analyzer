/*******************************************************************************
 * Copyright (c) 2021. ONERA
 * This file is part of PML Analyzer
 *
 * PML Analyzer is free software ;
 * you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation ;
 * either version 2 of  the License, or (at your option) any later version.
 *
 * PML  Analyzer is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY ;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this program ;
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA
 ******************************************************************************/

package views.interference.examples.simpleT1042

import pml.examples.simpleT1042.{SimpleSoftwareAllocation, SimpleT1042LibraryConfiguration, SimpleT1042Platform, SimpleT1042TransactionLibrary}
import pml.operators._
import views.interference.model.specification.{ApplicativeTableBasedInterferenceSpecification, PhysicalTableBasedInterferenceSpecification}
import views.interference.operators._

trait SimpleT1042PhysicalTableBasedInterferenceSpecification extends PhysicalTableBasedInterferenceSpecification {
  self: SimpleT1042Platform  =>

  for {
    l <- services
    r <- services
    if l != r
    if (l.hardwareOwnerIs(dma) && r.hardwareOwnerIs(config_bus)) || (l.hardwareOwner == r.hardwareOwner && !l.hardwareOwnerIs(bus))
  } yield {
    l interfereWith r
  }

  for {
    l <- transactions
    r <- transactions
    if l != r
    if l.initiator == r.initiator
  } yield {
    l exclusiveWith r
  }
}

trait SimpleT1042ApplicativeTableBasedInterferenceSpecification extends ApplicativeTableBasedInterferenceSpecification {
  self: SimpleT1042Platform with SimpleT1042TransactionLibrary with SimpleT1042LibraryConfiguration with SimpleSoftwareAllocation   =>

  app22 exclusiveWith app3
}
