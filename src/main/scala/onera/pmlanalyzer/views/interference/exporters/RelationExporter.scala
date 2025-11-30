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
import onera.pmlanalyzer.pml.model.configuration.TransactionLibrary
import onera.pmlanalyzer.pml.model.hardware.Platform
import onera.pmlanalyzer.pml.model.service.Service
import onera.pmlanalyzer.*
import onera.pmlanalyzer.views.interference.model.specification.InterferenceSpecification.AtomicTransactionId
import onera.pmlanalyzer.views.interference.model.specification.{
  ApplicativeTableBasedInterferenceSpecification,
  InterferenceSpecification
}

import java.io.FileWriter

private[exporters] object RelationExporter {

  private def getWriter(name: String): FileWriter = {
    val file = FileManager.exportDirectory.getFile(name)
    new FileWriter(file)
  }

  trait Ops {

    /** Implicit class used for method extension of platform to provide export
     * features
     *
     * @param self
     * the platform providing the export features
     */
    extension (self: Platform & InterferenceSpecification) {

      def exportServiceInterfereTable(): Unit = {
        import self.*
        val writer = getWriter(
          FileManager.getServiceInterfereTableName(self.fullName)
        )
        val table = relationToMap(self.services, finalInterfereWith)
        writer.write(
          "Service, Service(s)\n"
        )
        val toWrite = for {
          (k, v) <- table
        } yield {
          s"$k, ${v.map(_.toString).toSeq.sorted.mkString(", ")}\n"
        }
        toWrite.toSeq.sorted
          .foreach(writer.write)
        writer.flush()
        writer.close()
      }

      def exportAtomicTransactionExclusiveTable(): Unit = {
        val writer = getWriter(
          FileManager.getAtomicTransactionExclusiveTableName(self.fullName)
        )
        val table = self.relationToMap(
          self.purifiedAtomicTransactions.keySet,
          (l: AtomicTransactionId, r: AtomicTransactionId) =>
            self.finalExclusive(l, r)
        )
        writer.write(
          "AtomicTransactionId, AtomicTransactionId(s)\n"
        )
        val toWrite = for {
          (k, v) <- table
        } yield {
          s"$k, ${v.map(_.toString).toSeq.sorted.mkString(", ")}\n"
        }
        toWrite.toSeq.sorted
          .foreach(writer.write)
        writer.flush()
        writer.close()
      }

      def exportTransactionExclusiveTable(): Unit = {
        val writer = getWriter(
          FileManager.getTransactionExclusiveTableName(self.fullName)
        )
        val table = self.finalExclusive(self.purifiedTransactions.keySet)
        writer.write(
          "PhysicalTransactionId, PhysicalTransactionId(s)\n"
        )
        val toWrite = for {
          (k, v) <- table
        } yield {
          s"$k, ${v.map(_.toString).toSeq.sorted.mkString(", ")}\n"
        }
        toWrite.toSeq.sorted
          .foreach(writer.write)
        writer.flush()
        writer.close()
      }

      def exportAtomicTransactionTable(): Unit = {
        val writer = getWriter(
          FileManager.getAtomicTransactionTableName(self.fullName)
        )
        writer.write(
          "AtomicTransactionId, Path\n"
        )
        val toWrite = for {
          (a, p) <- self.purifiedAtomicTransactions
        } yield s"$a, ${p.mkString(" :: ")}\n"
        toWrite.toSeq.sorted
          .foreach(writer.write)
        writer.flush()
        writer.close()
      }

      def exportPhysicalTransactionTable(): Unit = {
        val writer = getWriter(
          FileManager.getPhysicalTransactionTableName(self.fullName)
        )
        writer.write(
          "PhysicalTransactionId, AtomicTransactionId(s)\n"
        )
        val toWrite = for {
          (k, v) <- self.purifiedTransactions
        } yield {
          s"$k, ${v.map(_.toString).toSeq.sorted.mkString(", ")}\n"
        }
        toWrite.toSeq.sorted
          .foreach(writer.write)
        writer.flush()
        writer.close()
      }
    }

    extension (
        self: Platform & TransactionLibrary & InterferenceSpecification
    ) {
      def exportUserTransactionTable(): Unit = {
        val writer = getWriter(
          FileManager.getUserTransactionTableName(self.fullName)
        )
        writer.write(
          "UserTransactionId, AtomicTransactionId(s)\n"
        )
        val purifiedAtomicTransactions = self.purifiedAtomicTransactions.keySet
        val toWrite = for {
          (k, v) <- self.transactionByUserName
          if v.subsetOf(purifiedAtomicTransactions)
        } yield {
          s"$k, ${v.map(_.toString).toSeq.sorted.mkString(", ")}\n"
        }
        toWrite.toSeq.sorted
          .foreach(writer.write)
        writer.flush()
        writer.close()
      }
    }

    extension (
        self: Platform & ApplicativeTableBasedInterferenceSpecification
    ) {
      def exportUserTransactionExclusiveTable(): Unit = {
        val writer = getWriter(
          FileManager.getUserTransactionExclusiveTableName(self.fullName)
        )
        writer.write(
          "UserTransactionId, UserTransactionId(s)\n"
        )
        val toWrite = for {
          (k, v) <- self.finalUserTransactionExclusive
        } yield {
          s"$k, ${v.map(_.toString).toSeq.sorted.mkString(", ")}\n"
        }
        toWrite.toSeq.sorted
          .foreach(writer.write)
        writer.flush()
        writer.close()
      }
    }
  }
}
