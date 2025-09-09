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

import onera.pmlanalyzer.pml.model.PMLNodeSetArbitrary
import onera.pmlanalyzer.pml.model.configuration.{
  ScenarioArbitrary,
  TransactionLibrary
}
import onera.pmlanalyzer.pml.model.hardware.CompositeArbitrary.given
import onera.pmlanalyzer.pml.model.relations.{
  LinkRelationArbitrary,
  RoutingRelationArbitrary,
  UseRelationArbitrary
}
import onera.pmlanalyzer.pml.model.service.*
import onera.pmlanalyzer.pml.model.software.*
import onera.pmlanalyzer.pml.model.utils.ArbitraryConfiguration
import onera.pmlanalyzer.pml.operators.*
import org.scalacheck.{Arbitrary, Gen}
import sourcecode.{File, Line}

object PlatformArbitrary {

  type PopulatedPlatform = Platform & ScenarioArbitrary & PMLNodeSetArbitrary &
    LinkRelationArbitrary & UseRelationArbitrary & RoutingRelationArbitrary

  given (using
      conf: ArbitraryConfiguration,
      line: Line,
      file: File
  ): Arbitrary[PopulatedPlatform] =
    Arbitrary(
      for {
        id <- Gen.identifier.suchThat(s => Platform.get(Symbol(s)).isEmpty)
        p = new Platform(Symbol(id), line, file)
          with TransactionLibrary
          with PMLNodeSetArbitrary
          with ApplicationArbitrary
          with DataArbitrary
          with LoadArbitrary
          with StoreArbitrary
          with TargetArbitrary
          with SimpleTransporterArbitrary
          with InitiatorArbitrary
          with VirtualizerArbitrary
          with TransporterArbitrary
//          with TransactionArbitrary
          with ScenarioArbitrary
          with LinkRelationArbitrary
          with UseRelationArbitrary
          with RoutingRelationArbitrary
        _ <- {
          import p.given
          PMLNodeSetArbitrary[Application].arbitrary
        }
        _ <- {
          import p.given
          PMLNodeSetArbitrary[Data].arbitrary
        }
        _ <- {
          import p.given
          PMLNodeSetArbitrary[Initiator].arbitrary
        }
        _ <- {
          import p.given
          PMLNodeSetArbitrary[Transporter].arbitrary
        }
        _ <- {
          import p.given
          PMLNodeSetArbitrary[Target].arbitrary
        }
        _ <- {
          import p.given
          PMLNodeSetArbitrary[Composite].arbitrary
        }
      } yield {
        if (conf.showArbitraryInfo)
          println(
            s"[INFO] platform generated with ${p.hardware.size} components and ${p.applications.size} applications"
          )
        p
      }
    )
}
