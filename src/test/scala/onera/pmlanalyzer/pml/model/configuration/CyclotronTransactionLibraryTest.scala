/** *****************************************************************************
 * Copyright (c) 2023. ONERA This file is part of PML Analyzer
 *
 * PML Analyzer is free software ; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation ; either version 2 of the License, or (at your
 * option) any later version.
 *
 * PML Analyzer is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY ; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program ; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package onera.pmlanalyzer.pml.model.configuration

import onera.pmlanalyzer.pml.model.instances.Cyclotron.CyclotronInstances
import onera.pmlanalyzer.views.interference.model.specification.InterferenceSpecification
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should
import onera.pmlanalyzer.views.interference.InterferenceTestExtension.FastTests

import scala.language.postfixOps

class CyclotronTransactionLibraryTest extends AnyFlatSpec with should.Matchers {

  "Cyclotron instances" should "contain exactly one transaction" taggedAs FastTests in {
    for { p <- CyclotronInstances.all } {
      p.transactionByUserName.size should be(1)
      p.scenarioByUserName.size should be(1)
      p.transactions.size should be(1)

      p.transactionByUserName.values.toSet should be(p.transactions)

      for {
        (name, path) <- p.transactionByUserName
        trPhysicalPath <- p.usedTr.toPhysical
      } {
        name should be(p.tr.userName)
        path should be(trPhysicalPath)
      }

      for {
        (name, path) <- p.scenarioByUserName
        trPhysicalPath <- p.usedTr.toPhysical
      } {
        name should not be (p.tr.userName)
        path should be(Set(trPhysicalPath))
      }
    }
  }
}
