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

package onera.pmlanalyzer.pml.model.service

import onera.pmlanalyzer.pml.model.PMLNodeBuilder
import onera.pmlanalyzer.pml.model.hardware.{Composite, ContainerLike, Platform}
import onera.pmlanalyzer.pml.model.utils.ReflexiveInfo
import org.scalacheck.{Arbitrary, Gen}
trait StoreArbitrary {
  self: ContainerLike =>

  given (using r: ReflexiveInfo): Arbitrary[Store] = Arbitrary(
    for {
      name <- Gen.identifier
        .map(x => Symbol(x))
        .suchThat(s =>
          Store.get(PMLNodeBuilder.formatName(s, currentOwner)).isEmpty
        )
    } yield Store(PMLNodeBuilder.formatName(name, currentOwner))
  )

}
