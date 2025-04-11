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
import onera.pmlanalyzer.pml.operators.*
import onera.pmlanalyzer.pml.model.hardware.Platform
import onera.pmlanalyzer.pml.model.software.{Application, ApplicationArbitrary, Data, DataArbitrary}
import onera.pmlanalyzer.pml.model.utils.ReflexiveInfo
import org.scalacheck.{Arbitrary, Gen}

trait TransactionArbitrary {
  self:Platform with TransactionLibrary =>

  given (using r:ReflexiveInfo): Arbitrary[Transaction] = Arbitrary(
    for {
      app <- Gen.oneOf(Application.all)
      data <- Gen.oneOf(Data.all)
      name <- Gen.identifier
      isRead <- Gen.prob(0.5)
    } yield
      if(isRead)
        Transaction(name, app read data)
      else
        Transaction(name, app write data)
  )

  given (using t:Arbitrary[Transaction], r:ReflexiveInfo) : Arbitrary[Scenario] = Arbitrary(
    for {
      tSeq <- Gen.listOf(t.arbitrary)
      name <- Gen.identifier
    } yield
      Scenario(name, tSeq:_*)
  )

}
