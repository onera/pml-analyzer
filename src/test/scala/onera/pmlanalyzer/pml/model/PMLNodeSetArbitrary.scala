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

package onera.pmlanalyzer.pml.model

import onera.pmlanalyzer.pml.model.hardware.*
import onera.pmlanalyzer.pml.model.software.*
import onera.pmlanalyzer.pml.model.utils.ArbitraryConfiguration
import org.scalacheck.{Arbitrary, Gen}

trait PMLNodeSetArbitrary {

  given [T <: PMLNode](using
      arb: Arbitrary[T],
      c: ArbitraryConfiguration
  ): Arbitrary[Set[T]] = Arbitrary(
    for {
      h <- arb.arbitrary
      max = {
        h match
          case _: Transporter => c.maxTransporterInContainer
          case _: Target      => c.maxTargetInContainer
          case _: Initiator   => c.maxInitiatorInContainer
          case _: Application => c.maxApplication
          case _: Data        => c.maxData
          case _: Composite   => c.maxCompositePerContainer
          case _              => 10
      }
      n <- Gen.choose(0, max)
      l <- Gen
        .listOfN(
          n,
          arb.arbitrary
        )
        .map(_.toSet)
    } yield l + h
  )
}

object PMLNodeSetArbitrary {
  def apply[T <: PMLNode](using ev: Arbitrary[Set[T]]): Arbitrary[Set[T]] = ev
}
