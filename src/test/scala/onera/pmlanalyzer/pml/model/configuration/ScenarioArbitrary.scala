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

import onera.pmlanalyzer.pml.model.PMLNodeBuilder
import onera.pmlanalyzer.pml.model.hardware.Platform
import onera.pmlanalyzer.pml.model.utils.ReflexiveInfo
import org.scalacheck.{Arbitrary, Gen}

import scala.annotation.targetName

trait ScenarioArbitrary {
  self: Platform with TransactionLibrary =>

  @targetName("given_Option_Scenario")
  given (using
      arbTr: Arbitrary[Option[Transaction]],
      r: ReflexiveInfo
  ): Arbitrary[Option[Scenario]] = Arbitrary(
    for {
      tT <- Gen.nonEmptyListOf(arbTr.arbitrary)
      name <- Gen.identifier.suchThat(s =>
        Scenario.get(PMLNodeBuilder.formatName(s, currentOwner)).isEmpty
      )
      tSeq = tT.flatten
    } yield
      if (tSeq.isEmpty)
        None
      else
        Some(Scenario(name, tSeq: _*))
  )

  @targetName("given_Option_UserScenario")
  given (using
      arbSc: Arbitrary[Option[Scenario]],
      r: ReflexiveInfo
  ): Arbitrary[Option[UsedScenario]] = Arbitrary(
    for {
      oS <- arbSc.arbitrary
      s <- oS
    } yield s.used
  )

}
