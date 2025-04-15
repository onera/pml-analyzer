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
import onera.pmlanalyzer.pml.model.utils.{All, ArbitraryConfiguration}
import onera.pmlanalyzer.pml.operators.*
import onera.pmlanalyzer.views.interference.InterferenceTestExtension.UnitTests
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import PlatformArbitrary.given
import PlatformArbitrary.PopulatedPlatform

class LinkRelationTest
    extends AnyFlatSpec
    with ScalaCheckPropertyChecks
    with should.Matchers {

  implicit val newConf: ArbitraryConfiguration = ArbitraryConfiguration.default
    .copy(maxVirtualizerLoad = 5)

  /**
   * This test first create a platform
   * then it build a random link relation on it and test the link and unlink
   */
  "LinkRelation" should "record properly link and unlink" taggedAs UnitTests in {
    forAll(minSuccessful(10)) { (p: PopulatedPlatform) =>
      {
        import p.given
        import p._
        forAll(minSuccessful(20)) { (m: Map[Hardware, Set[Hardware]]) =>
          {
            applyAll(m, link = true)
            PLLinkableToPL.edges should equal(m)
            ServiceLinkableToService.edges should equal(toServiceMap(m))
            applyAll(m, link = false)
            PLLinkableToPL.edges should be(empty)
            ServiceLinkableToService.edges should be(empty)
          }
        }
      }
    }
  }
}
