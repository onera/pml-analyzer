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

package onera.pmlanalyzer.views.patterns.exporters

import onera.pmlanalyzer.pml.exporters.FileManager
import onera.pmlanalyzer.views.patterns.model.*

import java.io.{File, FileWriter}

private[pmlanalyzer] trait LatexCodePrinter

private[pmlanalyzer] object LatexCodePrinter {

  trait Ops {
    extension (conclusion: Claim) {

      def printCode(): File = {
        val file = {
          for (s <- conclusion.short)
            yield FileManager.exportDirectory.getFile(s"${s}_text.tex")
        } getOrElse new File(s"${conclusion.label}_text.tex")
        val writer = new FileWriter(file)
        print(conclusion)(writer, 0)
        writer.close()
        file
      }

      private def print(
          s: String
      )(implicit printer: FileWriter, spacing: Int): Unit = {
        addSpacing
        s.split("""\\\\""").filterNot(_.isEmpty).toList match {
          case h :: Nil =>
            printer.write(s"\\textrm{${h.trim}}\\\\\n")
          case h :: t =>
            printer.write(s"\\textrm{$h}\\\\\n")
            t.foreach(e => {
              addSpacing
              printer.write(s"\\textrm{${e.trim}}\\\\\n")
            })
          case _ =>
        }
      }

      private def addSpacing(implicit
          printer: FileWriter,
          spacing: Int
      ): Unit = {
        for (_ <- 1 to spacing) printer.write("\t")
        if (spacing != 0) printer.write(s"\\hspace*{${5 * spacing}pt}")
      }

      private def print(
          claim: Claim
      )(implicit printer: FileWriter, spacing: Int): Unit = {
        addSpacing
        printer.write("\\texttt{Claim:} \\\\\n")
        print(s"${Claim.computeIdIn(conclusion)(claim)} ${claim.label}")(
          printer,
          spacing + 1
        )
        print(claim.strategy)
        print(claim.evidences.toList)
        claim.evidences foreach {
          case c: Claim =>
            printer.write("\n")
            print(c)
          case _ =>
        }
      }

      private def print(
          strategy: Strategy
      )(implicit printer: FileWriter, spacing: Int): Unit = {
        addSpacing
        printer.write("\\texttt{Strategy:} \\\\\n")
        print(s"${Claim.computeIdIn(conclusion)(strategy)} ${strategy.label}")(
          printer,
          spacing + 1
        )
        for (b <- strategy.backing) yield print(b)(printer, spacing + 1)
        for (b <- strategy.defeater) yield print(b)(printer, spacing + 1)
      }

      private def print(
          backing: Backing
      )(implicit printer: FileWriter, spacing: Int): Unit = {
        addSpacing
        printer.write("\\texttt{Backing:} \\\\\n")
        print(backing.label)(printer, spacing + 1)
      }

      private def print(
          defeater: Defeater
      )(implicit printer: FileWriter, spacing: Int): Unit = {
        addSpacing
        printer.write("\\texttt{Defeaters:} \\\\\n")
        print(defeater.label)(printer, spacing + 1)
      }

      private def print(
          evidences: List[Evidence]
      )(implicit printer: FileWriter, spacing: Int): Unit = {
        evidences collect { case g: Given => g } match {
          case Nil =>
          case l =>
            addSpacing
            printer.write("\\texttt{Givens}:\\\\\n")
            l foreach (g =>
              print(s"${Claim.computeIdIn(conclusion)(g)} ${g.label}")(
                printer,
                spacing + 1
              )
            )
        }
        evidences collect {
          case f: FinalEvidence => f;
          case c: Claim         => c
        } match {
          case Nil =>
          case l =>
            addSpacing
            printer.write("\\texttt{Evidences}:\\\\\n")
            l foreach (e => {
              print(s"${Claim.computeIdIn(conclusion)(e)} ${e.label}")(
                printer,
                spacing + 1
              )
            })
        }
      }
    }
  }
}
