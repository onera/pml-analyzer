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
import onera.pmlanalyzer.pml.model.hardware.{Initiator, Platform, Target}
import onera.pmlanalyzer.pml.model.relations.Endomorphism
import onera.pmlanalyzer.pml.model.software.{Application, Data}
import onera.pmlanalyzer.pml.model.utils.{Context, Owner, ReflexiveInfo}
import org.scalacheck.{Arbitrary, Gen}
import onera.pmlanalyzer.pml.model.utils.{
  All,
  ArbitraryConfiguration,
  Context,
  Owner,
  ReflexiveInfo
}
import onera.pmlanalyzer.pml.operators.*

import scala.annotation.targetName

trait ScenarioArbitrary {
  self: Platform with TransactionLibrary =>

  private lazy val linkedAppToData: Set[(Application, Data)] =
    for {
      i <- All[Initiator]
      if i.hostedApplications.nonEmpty
      t <- Endomorphism
        .closure(i, context.PLLinkableToPL.edges)
        .collect({ case x: Target => x })
      app <- i.hostedApplications
      d <- t.hostedData
    } yield app -> d

  private lazy val allAppToData: Set[(Application, Data)] =
    for {
      app <- All[Application]
      d <- All[Data]
    } yield app -> d

  private def simpleScenarioGenerator(using
      c: ArbitraryConfiguration,
      r: ReflexiveInfo
  ): Arbitrary[Option[Scenario]] = Arbitrary(
    {
      val validAD =
        if (c.discardImpossibleTransactions)
          linkedAppToData
        else
          allAppToData
      if (validAD.isEmpty)
        None
      else
        for {
          (app, data) <- Gen.oneOf(validAD)
          name <- Gen.identifier.suchThat(s =>
            Scenario
              .get(PMLNodeBuilder.formatName(Symbol(s), currentOwner))
              .isEmpty
          )
          isRead <- Gen.prob(0.5)
        } yield
          if (isRead)
            Some(Scenario(name, app read data))
          else
            Some(Scenario(name, app write data))
    }
  )

  @targetName("given_Option_Scenario")
  given (using
      r: ReflexiveInfo
  ): Arbitrary[Option[Scenario]] = Arbitrary(
    for {
      tT <- Gen.nonEmptyListOf(simpleScenarioGenerator.arbitrary)
      name <- Gen.identifier.suchThat(s =>
        Scenario.get(PMLNodeBuilder.formatName(Symbol(s), currentOwner)).isEmpty
      )
      tSeq = tT.flatten
    } yield {
      tSeq match {
        case ::(head, ::(second, next)) =>
          Some(Scenario(name, head, second, next: _*))
        case ::(head, Nil) => Some(head)
        case Nil           => None
      }
    }
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
