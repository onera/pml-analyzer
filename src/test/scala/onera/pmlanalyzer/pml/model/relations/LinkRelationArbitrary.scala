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

package onera.pmlanalyzer.pml.model.relations

import onera.pmlanalyzer.pml.model.hardware.*
import org.scalacheck.{Arbitrary, Gen}

trait LinkRelationArbitrary {
  self: Platform =>

  given (using
      arbI: Arbitrary[Initiator],
      arbTr: Arbitrary[Transporter],
      arbTg: Arbitrary[Target]
  ): Arbitrary[Map[Hardware, Set[Hardware]]] = Arbitrary(
    for {
      iSet <- Gen.listOfN(4, arbI.arbitrary).map(_.toSet).suchThat(_.nonEmpty)
      trSet <- Gen.listOfN(8, arbTr.arbitrary).map(_.toSet).suchThat(_.nonEmpty)
      tgSet <- Gen.listOfN(4, arbTg.arbitrary).map(_.toSet).suchThat(_.nonEmpty)
      map <- Gen.mapOf(
        Gen.zip(
          Gen.oneOf(trSet ++ iSet),
          Gen
            .atLeastOne(trSet ++ tgSet)
            .map(_.toSet)
        )
      )
    } yield map.filter((k, v) => !v.contains(k))
  )
}
