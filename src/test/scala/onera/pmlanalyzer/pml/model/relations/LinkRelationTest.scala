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
import onera.pmlanalyzer.pml.model.service.{
  LoadArbitrary,
  Service,
  StoreArbitrary
}
import onera.pmlanalyzer.pml.model.software.ApplicationArbitrary
import onera.pmlanalyzer.pml.operators.*
import onera.pmlanalyzer.views.interference.InterferenceTestExtension.UnitTests
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

class LinkRelationTest
    extends AnyFlatSpec
    with ScalaCheckPropertyChecks
    with should.Matchers {

  override implicit val generatorDrivenConfig: PropertyCheckConfiguration =
    PropertyCheckConfiguration(minSuccessful = 200)

  object LinkRelationTestFixture
      extends Platform(Symbol("LinkRelationTestFixture"))
      with ApplicationArbitrary
      with LoadArbitrary
      with StoreArbitrary
      with TargetArbitrary
      with SimpleTransporterArbitrary
      with InitiatorArbitrary
      with VirtualizerArbitrary
      with TransporterArbitrary
      with LinkRelationArbitrary

  import LinkRelationTestFixture.given
  import LinkRelationTestFixture._

  private def getServiceMap(m: Map[Hardware, Set[Hardware]]): Map[Service,Set[Service]] = {
    val r = for {
      (k,v) <- m.toSeq
      kL <- k.loads
      kS <- k.stores
      x <- v
      xL <- x.loads
      xS <- x.stores
      (nK, nV) <- List(kL -> xL, kS -> xS)
    } yield
      nK -> nV
    r.groupMap(_._1)(_._2).view.mapValues(_.toSet).toMap
  }
  private def applyAll(m: Map[Hardware, Set[Hardware]], link:Boolean): Unit =
    for {
      (l, v) <- m
    } {
      l match
        case i: Initiator =>
          for { r <- v }
            r match
              case tr: Transporter =>
                if (link)
                  i link tr
                else
                  i unlink tr
              case tg: Target =>
                if(link)
                  i link tg
                else
                  i unlink tg
              case _ =>
        case tr: Transporter =>
          for { r <- v }
            r match
              case rTr: Transporter =>
                if (link)
                  tr link rTr
                else
                  tr unlink rTr
              case tg: Target =>
                if(link)
                  tr link tg
                else
                  tr unlink tg
              case _ =>
        case _ =>
    }
  
  "LinkRelation" should "record properly link and unlink" taggedAs UnitTests in {
    forAll { (m: Map[Hardware, Set[Hardware]]) =>
      {
        applyAll(m, link= true)
        PLLinkableToPL.edges should equal(m)
        ServiceLinkableToService.edges should equal (getServiceMap(m))
        applyAll(m, link= false)
        PLLinkableToPL.edges should be (empty)
        ServiceLinkableToService.edges should be (empty)
      }
    }
  }
}
