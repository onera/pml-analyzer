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
import onera.pmlanalyzer.pml.model.hardware.PlatformArbitrary.{*, given}
import onera.pmlanalyzer.pml.model.service.*
import onera.pmlanalyzer.pml.model.utils.ArbitraryConfiguration
import onera.pmlanalyzer.pml.operators.*
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

class RoutingRelationTest
    extends AnyFlatSpec
    with ScalaCheckPropertyChecks
    with should.Matchers {

  private def checkRoutingRelation(
      p: PopulatedPlatform,
      expected: Map[(Initiator, Target, Hardware), Hardware]
  ): Unit = {
    import p.*
    val expectedService = p.toServiceRouting(expected)
    for {
      (k, v) <- context.InitiatorRouting.edges
    } {
      if (expectedService.contains(k))
        v should equal(expectedService(k))
      else
        v should be(empty)
    }
  }

  "RoutingRelation" should "encode properly route constraints" in {
    ArbitraryConfiguration.default
      .copy(removeUnreachableLink = true)
    forAll(minSuccessful(10)) { (p: PopulatedPlatform) =>
      import p.{*, given}
      forAll(minSuccessful(5)) {
        (
            link: Map[Hardware, Set[Hardware]],
            use: Map[Initiator, Set[Service]]
        ) =>
          applyAllLinks(link, undo = false)
          applyAllUses(use, undo = false)
          forAll(minSuccessful(5)) {
            (routing: Map[(Initiator, Target, Hardware), Hardware]) =>
              applyAllRoute(routing, undo = false)
              checkRoutingRelation(p, routing)
              applyAllRoute(routing, undo = true)
              checkRoutingRelation(p, routing)
          }
          applyAllUses(use, undo = true)
          applyAllLinks(link, undo = true)
      }
    }
  }
}
