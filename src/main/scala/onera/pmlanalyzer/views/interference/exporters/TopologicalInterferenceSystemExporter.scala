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

import onera.pmlanalyzer.pml.model.configuration.TransactionLibrary
import onera.pmlanalyzer.pml.model.hardware.Platform
import onera.pmlanalyzer.views.interference.model.specification.{
  ApplicativeTableBasedInterferenceSpecification,
  InterferenceSpecification
}
import onera.pmlanalyzer.*

object TopologicalInterferenceSystemExporter {

  trait Ops {
    extension (self: Platform & InterferenceSpecification) {
      def exportTopologicalInterferenceSystem(): Unit = {
        self.exportAtomicTransactionTable()
        self.exportPhysicalTransactionTable()
        self.exportAtomicTransactionExclusiveTable()
        self.exportTransactionExclusiveTable()
        self.exportServiceInterfereTable()
        self match {
          case l: (Platform & InterferenceSpecification & TransactionLibrary) =>
            l.exportUserTransactionTable()
          case _ =>
        }
        self match {
          case l: (Platform & ApplicativeTableBasedInterferenceSpecification) =>
            l.exportUserTransactionExclusiveTable()
          case _ =>
        }
      }
    }
  }
}
