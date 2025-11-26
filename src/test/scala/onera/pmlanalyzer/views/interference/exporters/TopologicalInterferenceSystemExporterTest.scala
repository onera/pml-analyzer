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

import onera.pmlanalyzer.pml.exporters.FileManager
import onera.pmlanalyzer.pml.model.instances.keystone.KeystoneWithRosace
import onera.pmlanalyzer.pml.model.instances.mySys.MySys
import onera.pmlanalyzer.pml.operators.*
import onera.pmlanalyzer.views.interference.operators.*
import onera.pmlanalyzer.views.interference.InterferenceTestExtension.FastTests
import onera.pmlanalyzer.views.interference.model.formalisation.TopologicalInterferenceSystem
import onera.pmlanalyzer.views.interference.model.specification.ApplicativeTableBasedInterferenceSpecification
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should

class TopologicalInterferenceSystemExporterTest
    extends AnyFlatSpec
    with should.Matchers {

  for {
    (platform, expectedResultFileName) <- List(
      MySys -> "mySys",
      KeystoneWithRosace -> "keystone"
    )
  } yield {

    s"The topological interference system of ${platform.fullName}" should "be exportable and consistent with platform info" taggedAs FastTests in {
      platform.exportTopologicalInterferenceSystem()
      val TIS = TopologicalInterferenceSystem(
        platform.fullName,
        platform.initiators.size,
        platform.sourceFile
      )
      TIS should be(defined)
      for {
        importedTIS <- TIS
      } yield {
        importedTIS should equal(
          platform.computeTopologicalInterferenceSystem(
            platform.initiators.size
          )
        )
      }
    }

    it should "be consistent with expected tables" taggedAs FastTests in {
      val expectedTIS =
        for {
          atomicTransactionsS <- FileManager.extractResource(
            s"$expectedResultFileName/${FileManager.getAtomicTransactionTableName(platform.fullName)}"
          )
          idToTransactionS <- FileManager.extractResource(
            s"$expectedResultFileName/${FileManager.getPhysicalTransactionTableName(platform.fullName)}"
          )
          exclusiveWithATrS <- FileManager.extractResource(
            s"$expectedResultFileName/${FileManager.getAtomicTransactionExclusiveTableName(platform.fullName)}"
          )
          exclusiveWithTrS <- FileManager.extractResource(
            s"$expectedResultFileName/${FileManager.getTransactionExclusiveTableName(platform.fullName)}"
          )
          interfereWithS <- FileManager.extractResource(
            s"$expectedResultFileName/${FileManager.getServiceInterfereTableName(platform.fullName)}"
          )
        } yield {
          TopologicalInterferenceSystem(
            platform.fullName,
            platform.initiators.size,
            platform.sourceFile,
            atomicTransactionsS,
            idToTransactionS,
            exclusiveWithATrS,
            exclusiveWithTrS,
            interfereWithS,
            platform match {
              case _: ApplicativeTableBasedInterferenceSpecification =>
                FileManager.extractResource(
                  s"$expectedResultFileName/${FileManager.getUserTransactionExclusiveTableName(platform.fullName)}"
                )
              case _ => None
            },
            FileManager.extractResource(
              s"$expectedResultFileName/${FileManager.getUserTransactionTableName(platform.fullName)}"
            )
          )
        }
      platform.exportTopologicalInterferenceSystem()
      val foundTIS = TopologicalInterferenceSystem(
        platform.fullName,
        platform.initiators.size,
        platform.sourceFile
      )
      foundTIS should be(defined)
      expectedTIS should be(defined)
      foundTIS should equal(expectedTIS)
    }
  }

}
