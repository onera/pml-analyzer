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

import onera.pmlanalyzer.pml.model.software.*
import onera.pmlanalyzer.pml.model.service.*
import onera.pmlanalyzer.pml.model.relations.{
  LinkRelationArbitrary,
  RoutingRelationArbitrary,
  UseRelationArbitrary
}
import onera.pmlanalyzer.pml.model.utils.{All, ArbitraryConfiguration}
import org.scalacheck.{Arbitrary, Gen}
import sourcecode.{File, Line}
import CompositeArbitrary.given

object PlatformArbitrary {

  type PopulatedPlatform = Platform & LinkRelationArbitrary &
    UseRelationArbitrary & RoutingRelationArbitrary & All.Instances

  given (using
      conf: ArbitraryConfiguration,
      line: Line,
      file: File
  ): Arbitrary[PopulatedPlatform] =
    Arbitrary(
      for {
        id <- Gen.identifier.suchThat(s => Platform.get(Symbol(s)).isEmpty)
        p = new Platform(Symbol(id), line, file)
          with ApplicationArbitrary
          with DataArbitrary
          with LoadArbitrary
          with StoreArbitrary
          with TargetArbitrary
          with SimpleTransporterArbitrary
          with InitiatorArbitrary
          with VirtualizerArbitrary
          with TransporterArbitrary
          with LinkRelationArbitrary
          with UseRelationArbitrary
          with RoutingRelationArbitrary
          with PMLNodeArbitrary
          with All.Instances
        maxData <- Gen.choose(1, conf.maxData)
        _ <- {
          import p.given
          Gen.listOfN(
            maxData,
            summon[Arbitrary[Data]].arbitrary
          )
        }
        maxApp <- Gen.choose(1, conf.maxApplication)
        _ <- {
          import p.given
          Gen.listOfN(
            maxApp,
            summon[Arbitrary[Application]].arbitrary
          )
        }
        maxInitiatorInContainer <- Gen.choose(1, conf.maxInitiatorInContainer)
        _ <- {
          import p.given
          Gen.listOfN(
            maxInitiatorInContainer,
            summon[Arbitrary[Initiator]].arbitrary
          )
        }
        maxTransporterInContainer <- Gen.choose(1, conf.maxTransporterInContainer)
        _ <- {
          import p.given
          Gen.listOfN(
            maxTransporterInContainer,
            summon[Arbitrary[Transporter]].arbitrary
          )
        }
        maxTargetInContainer <- Gen.choose(1, conf.maxTargetInContainer)
        _ <- {
          import p.given
          Gen.listOfN(
            maxTargetInContainer,
            summon[Arbitrary[Target]].arbitrary
          )
        }
        maxCompositePerContainer <- Gen.choose(1, conf.maxCompositePerContainer)
        _ <- {
          import p.given
          Gen.listOfN(
            maxCompositePerContainer,
            summon[Arbitrary[Composite]].arbitrary
          )
        }
      } yield p
    )
}
