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

package onera.pmlanalyzer.views.interference.exporters

import onera.pmlanalyzer.pml.exporters.UMLExporter.DOTServiceOnly
import onera.pmlanalyzer.pml.exporters.{FileManager, UMLExporter}
import onera.pmlanalyzer.pml.model.hardware.Platform
import onera.pmlanalyzer.views.interference.model.specification.InterferenceSpecification
import onera.pmlanalyzer.views.interference.model.specification.InterferenceSpecification.{
  PhysicalScenarioId,
  multiTransactionId
}

import java.io.FileWriter

object InterferenceGraphExporter {
  trait Ops {
    extension (x: Platform with InterferenceSpecification) {
      def exportMultiTransactionGraph(it: Set[PhysicalScenarioId]): Unit = {
        val multiTransactionName = multiTransactionId(
          it.map(x => PhysicalScenarioId(x.id))
        )
        implicit val writer: FileWriter = new FileWriter(
          FileManager.exportDirectory.getFile(
            s"${x.fullName}_${
                if (multiTransactionName.id.name.length >= 100) multiTransactionName.hashCode.toString
                else multiTransactionName
              }.dot"
          )
        )
        DOTServiceOnly.resetService()
        writer.write(DOTServiceOnly.getHeader)

        val serviceAssociations = for {
          s <- it
          t <- x.purifiedScenarios(s)
          l = x
            .purifiedTransactions(t)
            .sliding(2)
            .collect { case Seq(f, t) => f -> t }
            .toList
          if l.nonEmpty
          (l, r) <- l
          as <- DOTServiceOnly.getAssociation(l, r, "")
        } yield as

        val services = it
          .flatMap(x.purifiedScenarios)
          .flatMap(x.purifiedTransactions)

        val interfereAssociations =
          (for {
            s <- services.subsets(2) if x.finalInterfereWith(s.head, s.last)
            as <- DOTServiceOnly.getAssociation(
              s.head,
              s.last,
              "interfere"
            )
          } yield as).toSeq

        DOTServiceOnly.exportUML(
          x,
          interfereAssociations ++ serviceAssociations
        )
        writer.write(DOTServiceOnly.getFooter)
        writer.close()
      }
    }
  }
}
