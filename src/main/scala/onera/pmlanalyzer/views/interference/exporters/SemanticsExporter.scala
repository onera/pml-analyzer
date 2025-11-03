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
import onera.pmlanalyzer.pml.model.hardware.{Hardware, Platform}
import onera.pmlanalyzer.pml.operators.*
import onera.pmlanalyzer.views.interference.model.formalisation.SolverImplm
import onera.pmlanalyzer.views.interference.model.formalisation.SolverImplm.Monosat
import onera.pmlanalyzer.views.interference.operators.*

import java.io.{File, FileWriter}
import scala.concurrent.Await
import scala.concurrent.duration.DurationInt
import scala.io.Source
import scala.language.postfixOps

object SemanticsExporter {
  trait Ops {
    extension [T <: Platform](self: T) {

      def exportSemanticsSize(ignoreExistingFiles: Boolean = false)(using
          ev: Analyse[T],
          p: Provided[T, Hardware]
      ): File = {
        val semantics =
          self.getSemanticsSize(ignoreExistingFiles).toSeq.sortBy(_._1)
        val file = FileManager.exportDirectory.getFile(
          FileManager.getSemanticSizeFileName(self)
        )
        val writer = new FileWriter(file)
        writer.write("Multi-transaction cardinal, Number\n")
        for ((i, n) <- semantics)
          writer.write(s"$i, $n\n")
        writer.close()
        file
      }

      def exportSemanticReduction(implm: SolverImplm = Monosat)(using
          ev: Analyse[T],
          p: Provided[T, Hardware]
      ): File = {
        val file = FileManager.exportDirectory.getFile(
          FileManager.getSemanticsReductionFileName(self)
        )
        val writer = new FileWriter(file)
        writer.write("Semantics Reduction is\n")
        writer.write(self.computeSemanticReduction(implm).toString())
        writer.close()
        file
      }
    }
  }
}
