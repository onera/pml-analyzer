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

package onera.pmlanalyzer.views.interference.model.formalisation.Petri

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should
import onera.pmlanalyzer.pml.examples.mySys.MySysExport.MySys

class MySysTest extends AnyFlatSpec with should.Matchers {

  import MySys.*

  "MySys" should "be transformable into PetriNet" in {
    for {
      (sId, tIds) <- transactionByUserName
    } {
      println(s"$sId contains:")
      for {
        tId <- tIds
        sPath <- purifiedTransactions.get(tId)
        tName <- transactionUserName.getOrElse(tId, Set(tId))
      } {
        println(s"$tName: ${sPath.mkString("::")}")
      }
    }
  }
}
