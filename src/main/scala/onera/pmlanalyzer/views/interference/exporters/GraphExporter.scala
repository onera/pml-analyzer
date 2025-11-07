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

import onera.pmlanalyzer.pml.exporters.PMLNodeGraphExporter.DOTServiceOnly
import onera.pmlanalyzer.pml.exporters.{FileManager, PMLNodeGraphExporter}
import onera.pmlanalyzer.pml.model.configuration.TransactionLibrary
import onera.pmlanalyzer.pml.model.configuration.TransactionLibrary.UserTransactionId
import onera.pmlanalyzer.pml.model.hardware.Platform
import onera.pmlanalyzer.views.interference.model.formalisation.InterferenceCalculusProblem.Method
import onera.pmlanalyzer.views.interference.model.formalisation.InterferenceCalculusProblem.Method.Default
import onera.pmlanalyzer.views.interference.model.formalisation.SolverImplm
import onera.pmlanalyzer.views.interference.model.formalisation.SolverImplm.Monosat
import onera.pmlanalyzer.views.interference.model.specification.InterferenceSpecification
import onera.pmlanalyzer.views.interference.model.specification.InterferenceSpecification.{
  PhysicalTransactionId,
  multiTransactionId
}
import onera.pmlanalyzer.views.interference.operators.Analyse
import onera.pmlanalyzer.views.interference.operators.*

import java.io.{File, FileWriter}

object GraphExporter {
  trait Ops {
    extension [T <: Platform with InterferenceSpecification](self: T) {

      def exportGraphReduction(
          implm: SolverImplm = Monosat,
          method: Method = Default
      )(using ev: Analyse[T]): File = {
        val file = FileManager.exportDirectory.getFile(
          FileManager.getGraphReductionFileName(self)
        )
        val writer = new FileWriter(file)
        writer.write("Graph Reduction is\n")
        writer.write(
          self.computeGraphReduction(implm, method).toString()
        )
        writer.close()
        file
      }

      def exportAnalysisGraph(
          implm: SolverImplm = Monosat,
          method: Method = Default
      )(using
          ev: Analyse[T]
      ): File =
        ev.printGraph(self, implm, method)

      def exportInterferenceGraphFromString(
          it: Set[String],
          additionalName: Option[String] = None
      ): Option[File] = {
        val fromPhyTr =
          for {
            tr <- it
            id = self.purifiedTransactions.keySet.find(_.id.name == tr)
          } yield {
            id.orElse(
              self match {
                case lib: TransactionLibrary =>
                  val atomic =
                    lib.transactionByUserName(UserTransactionId(Symbol(tr)))
                  for {
                    (id, _) <- self.purifiedTransactions.find(_._2 == atomic)
                  } yield id
                case _ => None
              }
            )
          }
        val found = fromPhyTr.flatten
        if (found.size == fromPhyTr.size)
          Some(exportInterferenceGraph(found, additionalName))
        else
          None
      }

      // FIXME Add two versions one with coloring of edge per transaction, the other with transaction labelled to edges
      // remove interfere on service from the same initiation
      // add edge labelled with plus for non-atomic multi-transaction
      def exportInterferenceGraph(
          it: Set[PhysicalTransactionId],
          additionalName: Option[String] = None
      ): File = {
        val multiTransactionName = multiTransactionId(
          it.map(x => PhysicalTransactionId(x.id))
        )
        val add = additionalName match {
          case Some(value) => s"_$value"
          case None        => ""
        }
        val file = FileManager.exportDirectory.getFile(
          s"${self.fullName}${add}_${multiTransactionName.hashCode}.dot"
        )
        implicit val writer: FileWriter = new FileWriter(file)
        DOTServiceOnly.resetService()
        writer.write(DOTServiceOnly.getHeader)

        val serviceAssociations = for {
          s <- it
          t <- self.purifiedTransactions(s)
          l = self
            .purifiedAtomicTransactions(t)
            .sliding(2)
            .collect { case Seq(f, t) => f -> t }
            .toList
          if l.nonEmpty
          (l, r) <- l
          as <- DOTServiceOnly.getAssociation(l, r, "")
        } yield as

        val services = it
          .flatMap(self.purifiedTransactions)
          .flatMap(self.purifiedAtomicTransactions)

        val interfereAssociations =
          (for {
            s <- services.subsets(2) if self.finalInterfereWith(s.head, s.last)
            as <- DOTServiceOnly.getAssociation(
              s.head,
              s.last,
              "interfere"
            )
          } yield as).toSeq

        DOTServiceOnly.exportGraph(
          self,
          interfereAssociations ++ serviceAssociations
        )
        writer.write(DOTServiceOnly.getFooter)
        writer.close()
        file
      }
    }
  }
}
