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
import onera.pmlanalyzer.pml.model.utils.{
  All,
  ArbitraryConfiguration,
  Context,
  Owner,
  ReflexiveInfo
}
import onera.pmlanalyzer.pml.operators.*
import org.scalacheck.{Arbitrary, Gen}

import scala.annotation.targetName

object TransactionArbitrary {

  @targetName("given_Option_Transaction")
  given (using
      context: Context,
      currentOwner: Owner,
      c: ArbitraryConfiguration,
      r: ReflexiveInfo
  ): Arbitrary[Option[Transaction]] = Arbitrary(
    {
      val validAD =
        if (c.discardImpossibleTransactions)
          for {
            app <- All[Application]
            i <- app.hostingInitiators
            t <- Endomorphism
              .closure(i, context.PLLinkableToPL.edges)
              .collect({ case x: Target => x })
            d <- t.hostedData
          } yield app -> d
        else
          for {
            app <- All[Application]
            d <- All[Data]
          } yield app -> d

      if (validAD.isEmpty)
        None
      else
        for {
          (app, data) <- Gen.oneOf(validAD)
          name <- Gen.identifier.suchThat(s =>
            Transaction
              .get(PMLNodeBuilder.formatName(Symbol(s), currentOwner))
              .isEmpty
          )
          isRead <- Gen.prob(0.5)
        } yield
          if (isRead)
            Some(Transaction(name, app read data))
          else
            Some(Transaction(name, app write data))
    }
  )

  @targetName("given_Option_UsedTransaction")
  given (using
      context: Context,
      arbTr: Arbitrary[Option[Transaction]],
      r: ReflexiveInfo
  ): Arbitrary[Option[UsedTransaction]] = Arbitrary(
    for {
      oT <- arbTr.arbitrary
      t <- oT
    } yield t.used
  )
}
