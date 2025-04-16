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
import onera.pmlanalyzer.pml.model.relations.{LinkRelationArbitrary, RoutingRelationArbitrary, UseRelationArbitrary}
import onera.pmlanalyzer.pml.model.utils.{All, ArbitraryConfiguration}
import org.scalacheck.{Arbitrary, Gen}
import sourcecode.{File, Line}

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
        _ <- {
          import p.given
          summon[Arbitrary[Set[Data]]].arbitrary
        }
        _ <- {
          import p.given
          summon[Arbitrary[Set[Application]]].arbitrary
        }
        _ <- {
          import p.given
          summon[Arbitrary[Set[Initiator]]].arbitrary
        }
        _ <- {
          import p.given
          summon[Arbitrary[Set[Transporter]]].arbitrary
        }
        _ <- {
          import p.given
          summon[Arbitrary[Set[Target]]].arbitrary
        }
      } yield p
    )
}
