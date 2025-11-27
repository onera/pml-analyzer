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
import onera.pmlanalyzer.pml.model.software.{Application, Data}
import onera.pmlanalyzer.pml.model.utils.{All, ArbitraryConfiguration}
import onera.pmlanalyzer.pml.operators.*
import onera.pmlanalyzer.views.interference.InterferenceTestExtension.UnitTests
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

class UseRelationTest
    extends AnyFlatSpec
    with ScalaCheckPropertyChecks
    with should.Matchers {

  private def checkUseRelation[K, V](
      expected: Map[K, Set[V]]
  )(using allK: All[K], used: Used[K, V]): Unit = {
    for {
      k <- allK()
    } {
      if (expected.contains(k))
        used(k) should be(expected(k))
      else
        used(k) should be(empty)
    }
  }

  "UseRelation" should "encode properly the use of services by initiators" taggedAs UnitTests in {
    forAll(minSuccessful(20)) { (p: PopulatedPlatform) =>
      import p.{*, given}
      implicit val conf: ArbitraryConfiguration = ArbitraryConfiguration.default
        .copy(forceTotalHosting = true)
      forAll(minSuccessful(20)) { (use: Map[Initiator, Set[Service]]) =>
        applyAllUses(use, undo = false)
        checkUseRelation(use)
        applyAllUses(use, undo = true)
        checkUseRelation[Initiator, Service](Map.empty)
      }
      Platform.clear()
    }
  }

  it should "encode properly the use of services by applications" taggedAs UnitTests in {
    forAll(minSuccessful(20)) { (p: PopulatedPlatform) =>
      import p.{*, given}
      forAll(minSuccessful(20)) { (use: Map[Application, Set[Service]]) =>
        applyAllUses(use, undo = false)
        checkUseRelation(use)
        applyAllUses(use, undo = true)
        checkUseRelation[Application, Service](Map.empty)
      }
      Platform.clear()
    }
  }

  it should "encode properly the hosting of applications on initiators" taggedAs UnitTests in {
    forAll(minSuccessful(20)) { (p: PopulatedPlatform) =>
      import p.{*, given}
      forAll(minSuccessful(20)) { (use: Map[Application, Set[Initiator]]) =>
        applyAllUses(use, undo = false)
        checkUseRelation(use)
        applyAllUses(use, undo = true)
        checkUseRelation[Application, Initiator](Map.empty)
      }
      Platform.clear()
    }
  }

  it should "encode properly the hosting of data on targets" taggedAs UnitTests in {
    forAll(minSuccessful(20)) { (p: PopulatedPlatform) =>
      import p.{*, given}
      forAll(minSuccessful(20)) { (use: Map[Data, Set[Target]]) =>
        applyAllUses(use, undo = false)
        checkUseRelation(use)
        applyAllUses(use, undo = true)
        checkUseRelation[Data, Target](Map.empty)
      }
      Platform.clear()
    }
  }
}
