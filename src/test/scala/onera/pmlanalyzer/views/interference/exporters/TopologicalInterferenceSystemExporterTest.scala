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

package onera.pmlanalyzer.views.interference.exporters

import onera.pmlanalyzer.pml.model.instances.mySys.MySys
import onera.pmlanalyzer.pml.operators.*
import onera.pmlanalyzer.views.interference.InterferenceTestExtension.FastTests
import onera.pmlanalyzer.views.interference.model.formalisation.TopologicalInterferenceSystem
import onera.pmlanalyzer.views.interference.operators.PostProcess
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should

class TopologicalInterferenceSystemExporterTest extends AnyFlatSpec with should.Matchers {

  MySys.fullName should "be able to export the corresponding topological interference system" taggedAs FastTests in {
    MySys.exportTopologicalInterferenceSystem()
    val TIS = TopologicalInterferenceSystem(MySys.fullName, MySys.initiators.size, MySys.sourceFile)
    TIS should be (defined)
    for {
      importedTIS <- TIS
    } yield {
      val expectedExclusiveWithATr =
        MySys.relationToMap(
          MySys.purifiedAtomicTransactions.keySet,
          (l, r) => MySys.finalExclusive(l, r)
        )
      val expectedExclusiveWithTr =
        MySys.relationToMap(
          MySys.purifiedTransactions.keySet,
          (l, r) => MySys.finalExclusive(l, r)
        )
      val expectedInterfereWith =
        MySys.relationToMap(
          MySys.services,
          (l, r) => MySys.finalInterfereWith(l, r)
        ).map((k,v) => k.name -> v.map(_.name))

      importedTIS.atomicTransactions should equal (MySys.purifiedAtomicTransactions.transform((_, v) => v.map(_.name)))
      importedTIS.idToTransaction should equal (MySys.purifiedTransactions)
      importedTIS.exclusiveWithATr should equal (expectedExclusiveWithATr)
      importedTIS.exclusiveWithTr should equal (expectedExclusiveWithTr)
      importedTIS.interfereWith should equal (expectedInterfereWith)
      importedTIS.maxSize should equal (MySys.initiators.size)
      importedTIS.finalUserTransactionExclusiveOpt should equal (Some(MySys.finalUserTransactionExclusive))
      importedTIS.transactionUserNameOpt should equal (Some(MySys.transactionUserName))
      importedTIS.name should equal (MySys.fullName)
      importedTIS.sourceFile should equal (MySys.sourceFile)
    }
  }

}
