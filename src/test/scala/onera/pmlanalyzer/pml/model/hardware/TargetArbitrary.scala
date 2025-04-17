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

package onera.pmlanalyzer.pml.model.hardware

import onera.pmlanalyzer.pml.model.PMLNodeBuilder
import onera.pmlanalyzer.pml.model.service.{
  Load,
  LoadArbitrary,
  Store,
  StoreArbitrary
}
import onera.pmlanalyzer.pml.model.utils.ReflexiveInfo
import org.scalacheck.{Arbitrary, Gen}

trait TargetArbitrary {
  self: Platform =>

  val maxTargetLoad: Int = 3
  val maxTargetStore: Int = 3

  given (using
      genLoad: Arbitrary[Load],
      genStore: Arbitrary[Store],
      r: ReflexiveInfo
  ): Arbitrary[Target] = Arbitrary(
    for {
      name <- Gen.identifier
      loads <- Gen.listOfN(maxTargetLoad, genLoad.arbitrary)
      stores <- Gen.listOfN(maxTargetStore, genStore.arbitrary)
    } yield Target
      .get(PMLNodeBuilder.formatName(name, currentOwner))
      .getOrElse(Target(name, (loads ++ stores).toSet))
  )
}
