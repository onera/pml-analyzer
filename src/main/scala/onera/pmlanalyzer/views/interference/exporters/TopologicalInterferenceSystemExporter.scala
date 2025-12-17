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
import onera.pmlanalyzer.views.interference.model.specification.{ApplicativeTableBasedInterferenceSpecification, InterferenceSpecification}
import onera.pmlanalyzer.*
import onera.pmlanalyzer.views.interference.model.formalisation.DefaultInterferenceCalculusProblem

import java.io.{File, FileWriter}

private[exporters] object TopologicalInterferenceSystemExporter {

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
      def exportTopologicalInterferenceSystemAsJSON(): File = {
        val file = FileManager.exportDirectory.getFile(s"${self.fullName}.json")
        val writer = new FileWriter(file)
        val system = self.computeTopologicalInterferenceSystem(2)
        val transaction = system.idToTransaction.keySet.toSeq.sortBy(_.id.name).zipWithIndex.toMap
        val problem = DefaultInterferenceCalculusProblem(system)
        val exclusive = for{
          (t,id) <- transaction.toSeq.sortBy(_._1)
          s = system.exclusiveWithTr(t) - t
        } yield {
          id -> s.map(transaction).toSeq.sorted
        }
        val nodes = problem.graph.nodes.zipWithIndex.toMap
        val edges =
          for {
            e <- problem.graph.edges
          } yield
            Set(nodes(e.from), nodes(e.to))
        val names = for {
          (t,i) <- transaction.toSeq.sortBy(_._2)
        } yield i ->  s"\"$t\""
        val nodeToTrs =
          for {
            (n,trs) <- problem.nodeToTransaction.toSeq.sortBy(x => nodes(x._1))
          } yield {
            nodes(n) -> trs.map(transaction).toSeq.sorted
          }

        {
          problem.nodeToTransaction.toSeq
            .flatMap((k, v) => v.map(k -> _))
            .groupMapReduce(x => transaction(x._2))(x => Set(nodes(x._1)))((l, r) => l ++ r)
            .toSeq
            .sortBy(_._1)
            .map(_._2)
        }
        writer.write(
          s"""{
             |\t"exclusive": [
             |${exclusive.map((k,v)=> s"[$k, ${v.mkString("[", ", ", "]")}]").mkString("\t\t",",\n\t\t","")}
             |\t],
             |\t"edges": [
             |${edges.map(v=> v.toSeq.sorted.mkString("[", ", ", "]")).toSeq.sorted.mkString("\t\t", ",\n\t\t", "")}
             |\t],
             |\t"transactionsOf": [
             |${nodeToTrs.map((k,v)=> s"[$k, ${v.mkString("[", ", ", "]")}]").mkString("\t\t", ",\n\t\t", "")}
             |\t],
             |\t"names": [
             |${names.map((k,v)=> s"[$k, $v]").mkString("\t\t", ",\n\t\t", "")}
             |\t]
             |} """.stripMargin
        )
        writer.flush()
        writer.close()
        file
      }
    }
  }
}
