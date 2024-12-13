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

package onera.pmlanalyzer.views.patterns.exporters

import onera.pmlanalyzer.pml.exporters.FileManager
import onera.pmlanalyzer.views.patterns.model._

import java.io.{File, FileWriter}
import java.nio.file.Paths

object LatexDiagramPrinter {
  implicit class LatexDiagramPrinterConclusion(conclusion: Claim) {

    private val ids = Claim.computeIdIn(conclusion)

    val file: File = {
      for (s <- conclusion.short)
        yield FileManager.exportDirectory.getFile(s"${s}_diagram.tex")
    } getOrElse new File(s"${conclusion.label}_diagram.tex")

    private def getSize(textWidth: Option[Int]): String =
      (for (s <- textWidth) yield s"set width = $s cm,") getOrElse ""

    private def getImplementation(implementation: Option[String]): String =
      (for (i <- implementation) yield i) getOrElse ""

    private def writeString(
        s: String
    )(implicit printer: FileWriter, spacing: Int): Unit = {
      s.split("\n").foreach { chunk =>
        addSpacing
        printer.write(chunk + "\n")
      }
    }

    def printDiagram(): File = {
      val writer = new FileWriter(file)
      writer.write("""
           |\\ifdefined\\standalone
           |\\documentclass{standalone}
           |\\usepackage[utf8]{inputenc}
           |\\usepackage{tikz}
           |\\usetikzlibrary{shapes,arrows,backgrounds,positioning,calc, automata, shadows, backgrounds,fit, arrows,graphs, trees, decorations.pathreplacing, patterns}
           |\\usepackage{forest}
           |\\usepackage{hyperref}
           |\\usepackage{varwidth}
           |\\input{tikzStyle}
           |
          |\\begin{document}
          """.stripMargin)
      for (width <- conclusion.width) yield {
        writer.write(s"\\resizebox{$width px}{!}{")
      }
      writer.write("\\fi")
      writer.write(
        "\\begin{forest}\n\tfor tree = { edge = {latex-}, parent anchor=south, child anchor=north, l sep+=.5em, s sep+= 1em },\n"
      )
      print(conclusion)(writer, 1)
      writer.write("""\end{forest}""")
      writer.write("\\ifdefined\\standalone")
      for (_ <- conclusion.width) yield {
        writer.write("}")
      }
      writer.write("\\end{document}\n\\fi")
      writer.close()
      file
    }

    private def addSpacing(implicit printer: FileWriter, spacing: Int): Unit =
      (1 to spacing) foreach (_ => printer.write("\t"))

    private def print(
        claim: Claim
    )(implicit printer: FileWriter, spacing: Int): Unit = {
      writeString(
        s"[{${ids(claim)} ${claim.label + getImplementation(claim.implementation)}}, conclusion,  s sep-= 1.25em, calign=first, ${getSize(claim.textWidth)} \n"
      )
      for (b <- claim.strategy.defeater) yield print(b)(printer, spacing + 1)
      print(claim.strategy)(printer, spacing + 1)
      claim.evidences foreach (e => print(e)(printer, spacing + 2))
      writeString("]\n")(printer, spacing + 1)
      for (b <- claim.strategy.backing) yield print(b)(printer, spacing + 1)
      writeString("]\n")
    }

    private def print(
        strategy: Strategy
    )(implicit printer: FileWriter, spacing: Int): Unit = {
      writeString(
        s"[{${ids(strategy)} ${strategy.label} ${getImplementation(strategy.implementation)}}, strategy, ${getSize(strategy.textWidth)}\n"
      )
    }

    private def print(
        backing: Backing
    )(implicit printer: FileWriter, spacing: Int): Unit = {
      writeString(s"[{${backing.label}}, backing, no edge]\n")
    }

    private def print(
        defeater: Defeater
    )(implicit printer: FileWriter, spacing: Int): Unit = {
      writeString(s"[{${defeater.label}}, defeater, no edge]\n")
    }

    private def print(
        evidence: Evidence
    )(implicit printer: FileWriter, spacing: Int): Unit = evidence match {
      case e @ FinalEvidence(content, implementation, textWidth, None) =>
        writeString(
          s"[{${ids(e)} $content ${getImplementation(implementation)}}, conclusion, ${getSize(textWidth)}] \n"
        )
      case e @ FinalEvidence(content, implementation, textWidth, Some(refOf)) =>
        val refPrinter = LatexDiagramPrinterConclusion(refOf)
        val refPath =
          Paths.get(refPrinter.file.getName).toString.replace(".tex", ".pdf")
        refPrinter.printDiagram()
        writeString(
          s"[{\\href{$refPath}{${ids(e)}} $content  ${getImplementation(implementation)}}, conclusion, ${getSize(textWidth)}] \n"
        )
      case g @ Given(content, implementation, textWidth) =>
        writeString(
          s"[{${ids(g)} $content  ${getImplementation(implementation)}}, conclusion,  ${getSize(textWidth)}, dashed ] \n"
        )
      case c: Claim =>
        print(c)
    }
  }
}

trait LatexDiagramPrinter
