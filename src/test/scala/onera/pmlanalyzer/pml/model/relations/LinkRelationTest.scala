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

import onera.pmlanalyzer.*
import onera.pmlanalyzer.pml.model.hardware.PlatformArbitrary.{
  PopulatedPlatform,
  given
}
import onera.pmlanalyzer.pml.model.utils.{All, ArbitraryConfiguration}
import onera.pmlanalyzer.pml.operators.Linked
import onera.pmlanalyzer.views.interference.InterferenceTestExtension.UnitTests
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

class LinkRelationTest
    extends AnyFlatSpec
    with ScalaCheckPropertyChecks
    with should.Matchers {

  private def checkLinkRelation[T](
      expected: Map[T, Set[T]]
  )(using allT: All[T], l: Linked[T, T]): Unit = {
    for {
      t <- All[T]
    } {
      if (expected.contains(t))
        t.linked should equal(expected(t))
      else
        t.linked should be(empty)
    }
  }

  private def checkLinkRelations(
      p: PopulatedPlatform,
      expected: Map[Hardware, Set[Hardware]]
  ): Unit = {
    import p.{*, given}

    applyAllLinks(expected, undo = false)
    checkLinkRelation(expected)
    checkLinkRelation(linkToServiceMap(expected))
    applyAllLinks(expected, undo = true)
    checkLinkRelation[Hardware](Map.empty)
    checkLinkRelation[Service](Map.empty)
  }

  /**
   * This test first create a platform
   * then it build a random link relation on it and test the link and unlink
   */
  "LinkRelation" should "record properly link and unlink when restricted to reachable values" taggedAs UnitTests in {
    implicit val newConf: ArbitraryConfiguration =
      ArbitraryConfiguration.default
        .copy(removeUnreachableLink = true)
    forAll(minSuccessful(10)) { (p: PopulatedPlatform) =>
      {
        import p.given
        forAll(minSuccessful(20)) { (m: Map[Hardware, Set[Hardware]]) =>
          checkLinkRelations(p, m)
        }
        Platform.clear()
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
        Platform.clear()
      }
    }
  }
}
