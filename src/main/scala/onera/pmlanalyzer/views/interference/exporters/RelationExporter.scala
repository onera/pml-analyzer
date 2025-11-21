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
import onera.pmlanalyzer.pml.model.configuration.{Transaction, TransactionLibrary}
import onera.pmlanalyzer.pml.model.hardware.Platform
import onera.pmlanalyzer.pml.model.relations.Relation
import onera.pmlanalyzer.pml.model.service.Service
import onera.pmlanalyzer.pml.model.software.*
import onera.pmlanalyzer.pml.operators.*
import onera.pmlanalyzer.views.interference.model.specification.InterferenceSpecification.AtomicTransactionId
import onera.pmlanalyzer.views.interference.model.specification.{InterferenceSpecification, TableBasedInterferenceSpecification}

import java.io.FileWriter

object RelationExporter {
  
  trait Ops {

    /** Implicit class used for method extension of platform to provide export
     * features
     *
     * @param platform
     * the platform providing the export features
     */
    implicit class Ops(platform: Platform & InterferenceSpecification) {

      import platform.*

      private def getWriter(name: String): FileWriter = {
        val file = FileManager.exportDirectory.getFile(name)
        new FileWriter(file)
      }

      def exportServiceInterfereTable(): Unit = {
        val writer = getWriter(s"${platform.fullName}ServiceInterfere.txt")
        val table = platform.relationToMap(platform.services, platform.finalInterfereWith)
        writer.write(
          "Service, Service(s)\n"
        )
        val toWrite = for {
          (k, v) <- table
        } yield {
          s"$k, ${v.map(_.toString).toSeq.sorted.mkString(",")}\n"
        }
        toWrite.toSeq.sorted
          .foreach(writer.write)
        writer.flush()
        writer.close()
      }

      def exportAtomicTransactionExclusiveTable(): Unit = {
        val writer = getWriter(s"${platform.fullName}AtomicTransactionExclusive.txt")
        val table = platform.relationToMap(
          platform.purifiedAtomicTransactions.keySet,
          (l: AtomicTransactionId, r: AtomicTransactionId) => finalExclusive(l, r))
        writer.write(
          "AtomicTransactionId, AtomicTransactionId(s)\n"
        )
        val toWrite = for {
          (k, v) <- table
        } yield {
          s"$k, ${v.map(_.toString).toSeq.sorted.mkString(",")}\n"
        }
        toWrite.toSeq.sorted
          .foreach(writer.write)
        writer.flush()
        writer.close()
      }

      def exportTransactionExclusiveTable(): Unit = {
        val writer = getWriter(s"${platform.fullName}TransactionExclusive.txt")
        val table = platform.finalExclusive(platform.purifiedTransactions.keySet)
        writer.write(
          "PhysicalTransactionId, PhysicalTransactionId(s)\n"
        )
        val toWrite = for {
          (k, v) <- table
        } yield {
          s"$k, ${v.map(_.toString).toSeq.sorted.mkString(",")}\n"
        }
        toWrite.toSeq.sorted
          .foreach(writer.write)
        writer.flush()
        writer.close()
      }

      def exportTransactionTable(): Unit = {
        val writer = getWriter(s"${platform.fullName}TransactionTable.txt")
        writer.write(
          "Transaction Name, Transaction Path, SourceCodeFile, SourceCodeLine\n"
        )
        val toWrite = for {
          (sc,s) <- platform.purifiedTransactions
          t = s
            .map(purifiedAtomicTransactions)
            .map(x =>
              if (s.size <= 1)
                x.mkString("::")
              else
                x.mkString("(", "::", ")")
            )
            .toSeq
            .sorted
        } yield s"$sc, ${t.mkString("+")}\n"
        toWrite.toSeq.sorted
          .foreach(writer.write)
        writer.flush()
        writer.close()
      }
    }
  }
}
