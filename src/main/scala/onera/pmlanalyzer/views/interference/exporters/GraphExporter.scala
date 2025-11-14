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
import onera.pmlanalyzer.pml.operators.*
import onera.pmlanalyzer.views.interference.model.formalisation.InterferenceCalculusProblem.Method
import onera.pmlanalyzer.views.interference.model.formalisation.InterferenceCalculusProblem.Method.Default
import onera.pmlanalyzer.views.interference.model.formalisation.SolverImplm
import onera.pmlanalyzer.views.interference.model.formalisation.SolverImplm.Monosat
import onera.pmlanalyzer.views.interference.model.specification.InterferenceSpecification
import onera.pmlanalyzer.views.interference.model.specification.InterferenceSpecification.{
  AtomicTransactionId,
  PhysicalTransactionId,
  multiTransactionId
}
import onera.pmlanalyzer.views.interference.operators.Analyse
import onera.pmlanalyzer.views.interference.operators.*

import java.io.{File, FileWriter}
import java.text.NumberFormat
import java.util.Locale

object GraphExporter {

  private val colorMap = Map(
    0 -> "#8E5A68",
    1 -> "#7541A5",
    2 -> "#FAAD62",
    3 -> "#FFF07E",
    4 -> "#C8D75F",
    5 -> "#99AE55"
  ).withDefaultValue("")

  private def assignColor(
      s: Set[PhysicalTransactionId]
  ): Map[PhysicalTransactionId, String] = {
    (for {
      (tr, i) <- s.toSeq.sortBy(_.id.name).zipWithIndex
    } yield tr -> colorMap(i)).toMap
  }

  trait Ops {
    extension [T <: Platform with InterferenceSpecification](self: T) {

      def exportGraphReduction(
          implm: SolverImplm = Monosat,
          method: Method = Default
      )(using ev: Analyse[T]): File = {
        val file = FileManager.exportDirectory.getFile(
          FileManager.getGraphReductionFileName(self, Some(method), Some(implm))
        )
        val writer = new FileWriter(file)
        writer.write("Graph Reduction is\n")
        writer.write(
          self
            .computeGraphReduction(implm, method, ignoreExistingFile = true)
            .toString()
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
          additionalName: Option[String] = None,
          colored: Boolean = true,
          trIdOnLabel: Boolean = true
      ): Option[File] = {
        val fromPhyTr =
          for {
            tr <- it
            id = self.purifiedTransactions.keySet.find(_.id.name == tr)
          } yield {
            id.orElse(
              self match {
                case lib: TransactionLibrary =>
                  for {
                    atomic <- lib.transactionByUserName.get(
                      UserTransactionId(Symbol(tr))
                    )
                    (id, _) <- self.purifiedTransactions.find(_._2 == atomic)
                  } yield id
                case _ => None
              }
            )
          }
        val found = fromPhyTr.flatten
        if (found.size == fromPhyTr.size)
          Some(
            exportInterferenceGraph(found, additionalName, colored, trIdOnLabel)
          )
        else
          None
      }

      def exportInterferenceGraph(
          it: Set[PhysicalTransactionId],
          additionalName: Option[String] = None,
          colored: Boolean = true,
          trIdOnLabel: Boolean = true
      ): File = {
        val assignedColorMap = assignColor(it)
        val nameMap =
          self match {
            case lib: TransactionLibrary =>
              (for {
                tr <- it
                uIds = lib.transactionUserName.get(
                  self.purifiedTransactions(tr)
                )
              } yield {
                tr -> (uIds match {
                  case Some(value) if value.nonEmpty => value.map(_.id.name)
                  case _                             => Set(tr.id.name)
                })
              }).toMap
            case _ => (for { tr <- it } yield tr -> Set(tr.id.name)).toMap
          }

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
          tr <- it
          t <- self.purifiedTransactions(tr)
          l = self
            .purifiedAtomicTransactions(t)
            .sliding(2)
            .collect { case Seq(f, t) => f -> t }
            .toList
          if l.nonEmpty
          (l, r) <- l
          as <- DOTServiceOnly.getAssociation(
            l,
            r,
            tyype = if (trIdOnLabel) nameMap(tr).mkString(", ") else "",
            color = if (colored) assignedColorMap(tr) else ""
          )
        } yield as

        val mergedServiceAssociations = for {
          (ss, as) <- serviceAssociations.groupBy(a => Set(a.left, a.right))
          l = ss.head
          r = ss.last
          names = as.map(_.name)
          colors = as.map(_.color)
        } yield {
          val color =
            if (colored && colors.size >= 2) {
              val nf = NumberFormat.getNumberInstance(Locale.ENGLISH)
              nf.setMinimumFractionDigits(1)
              nf.setMaximumFractionDigits(1)
              val proportion = nf.format(BigDecimal(1) / colors.size)
              colors.mkString("\"", ":", s";$proportion\"")
            } else if (colored && colors.size == 1)
              s"\"${colors.head}\""
            else
              ""
          DOTServiceOnly.DOTAssociation(
            l,
            r,
            name = if (trIdOnLabel) names.mkString(" ", ", ", "") else "",
            color = color
          )
        }

        val nonAtomicTrAssociations =
          for {
            tr <- it
            atIds <- self
              .purifiedTransactions(tr)
              .subsets(2)
            l = self.purifiedAtomicTransactions(atIds.head).head
            r = self.purifiedAtomicTransactions(atIds.last).head
            if l != r
            a <- DOTServiceOnly.getAssociation(
              l,
              r,
              tyype = "+",
              color = if (colored) s"\"${assignedColorMap(tr)}\"" else ""
            )
          } yield a

        val servicesWOHead = {
          for {
            tr <- it
            atId <- self.purifiedTransactions(tr)
            s <- self.purifiedAtomicTransactions(atId).tail
          } yield s
        }

        val interfereAssociations =
          (for {
            s <- servicesWOHead.subsets(2)
            if self.finalInterfereWith(s.head, s.last)
            as <- DOTServiceOnly.getAssociation(
              s.head,
              s.last,
              tyype = "interfere",
              color = ""
            )
          } yield as).toSeq

        DOTServiceOnly.exportGraph(
          self,
          interfereAssociations ++ mergedServiceAssociations ++ nonAtomicTrAssociations
        )
        writer.write(DOTServiceOnly.getFooter)
        writer.close()
        file
      }
    }
  }
}
