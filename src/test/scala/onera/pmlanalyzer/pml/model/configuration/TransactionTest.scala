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

package onera.pmlanalyzer.pml.model.configuration

import onera.pmlanalyzer.pml.model.hardware.{Hardware, Initiator, Target}
import onera.pmlanalyzer.pml.model.hardware.PlatformArbitrary.PopulatedPlatform
import onera.pmlanalyzer.pml.model.utils.ArbitraryConfiguration
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import onera.pmlanalyzer.pml.model.hardware.PlatformArbitrary.given
import onera.pmlanalyzer.pml.model.relations.LinkRelationArbitrary
import onera.pmlanalyzer.pml.model.service.Service
import onera.pmlanalyzer.pml.model.software.{Application, Data}
import onera.pmlanalyzer.pml.operators.*

class TransactionTest
    extends AnyFlatSpec
    with ScalaCheckPropertyChecks
    with should.Matchers {

  "Transactions" can "be defined" in {
    implicit val newConf: ArbitraryConfiguration =
      ArbitraryConfiguration.default
        .copy(removeUnreachableLink = true)
        .copy(forceTotalHosting = true)
        .copy(discardImpossibleTransactions = true)
    forAll(minSuccessful(10)) { (p: PopulatedPlatform) =>
      {
        import p.{*, given}
        forAll(minSuccessful(5)) {
          (
              link: Map[Hardware, Set[Hardware]],
              useD: Map[Data, Set[Target]],
              useA: Map[Application, Set[Initiator]]
          ) =>
            {
              applyAllLinks(link, undo = false)
              applyAllUses(useD, undo = false)
              applyAllUses(useA, undo = false)
              forAll(minSuccessful(5)) { (s: Option[Transaction]) =>
                for {
                  t <- s
                } {
                  t.owner should be(currentOwner)
                  for {
                    a <- t.sw()
                  } {
                    useA.keySet should contain(a)
                  }
                  for {
                    (iS, tS) <- t.iniTgt()
                  } {
                    iS.hardwareOwner should equal(iS.initiatorOwner)
                    tS.hardwareOwner should equal(tS.targetOwner)
                    useD.values should contain(tS.targetOwner)
                  }
                }
              }
              applyAllLinks(link, undo = true)
              applyAllUses(useD, undo = true)
              applyAllUses(useA, undo = true)
            }
        }
      }
    }
  }
}
