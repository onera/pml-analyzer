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

import onera.pmlanalyzer.pml.model.instances.DbusC2D2B8.DbusC2D2B8
import onera.pmlanalyzer.views.interference.model.specification.TableBasedInterferenceSpecification
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import onera.pmlanalyzer.views.interference.InterferenceTestExtension.FastTests

class DbusC2D2B8TransactionLibraryTest
    extends AnyFlatSpec
    with ScalaCheckPropertyChecks
    with should.Matchers {

  DbusC2D2B8.fullName should "contain the expected numbers of transactions" taggedAs FastTests in {
    DbusC2D2B8.transactionByUserName.size should be(32)
    DbusC2D2B8.scenarioByUserName.size should be(36)
    DbusC2D2B8.transactions.size should be(36)
  }

}
