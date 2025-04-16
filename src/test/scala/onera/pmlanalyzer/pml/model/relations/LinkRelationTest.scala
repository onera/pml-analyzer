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
import onera.pmlanalyzer.pml.model.hardware.PlatformArbitrary.{
  PopulatedPlatform,
  given
}
import onera.pmlanalyzer.pml.model.utils.ArbitraryConfiguration
import onera.pmlanalyzer.views.interference.InterferenceTestExtension.UnitTests
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

class LinkRelationTest
    extends AnyFlatSpec
    with ScalaCheckPropertyChecks
    with should.Matchers {

  private def checkLinkRelation[T](
      found: LinkRelation[T],
      expected: Map[T, Set[T]]
  ): Unit = {
    for {
      (k, v) <- found.edges
    } {
      if (expected.contains(k))
        v should equal(expected(k))
      else
        v should be(empty)
    }
  }

  private def checkLinkRelations(
      p: PopulatedPlatform,
      m: Map[Hardware, Set[Hardware]]
  ): Unit = {
    import p.*
    applyAllLinks(m, undo = false)
    checkLinkRelation(PLLinkableToPL, m)
    checkLinkRelation(ServiceLinkableToService, linkToServiceMap(m))
    applyAllLinks(m, undo = true)
    checkLinkRelation(PLLinkableToPL, Map.empty)
    checkLinkRelation(ServiceLinkableToService, Map.empty)
  }

  /**
   * This test first create a platform
   * then it build a random link relation on it and test the link and unlink
   */
  "LinkRelation" should "record properly link and unlink when restricted to reachable links" taggedAs UnitTests in {
    implicit val newConf: ArbitraryConfiguration =
      ArbitraryConfiguration.default
        .copy(removeUnreachableLink = true)
    forAll(minSuccessful(10)) { (p: PopulatedPlatform) =>
      {
        import p.given
        forAll(minSuccessful(20)) { (m: Map[Hardware, Set[Hardware]]) =>
          checkLinkRelations(p, m)
        }
      }
    }
  }

  it should "record properly link and unlink even if unreachable links are considered" taggedAs UnitTests in {
    implicit val newConf: ArbitraryConfiguration =
      ArbitraryConfiguration.default
        .copy(removeUnreachableLink = false)
    forAll(minSuccessful(10)) { (p: PopulatedPlatform) =>
      {
        import p.given
        forAll(minSuccessful(20)) { (m: Map[Hardware, Set[Hardware]]) =>
          checkLinkRelations(p, m)
        }
      }
    }
  }
}
