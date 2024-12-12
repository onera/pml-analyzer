package onera.pmlanalyzer

import onera.pmlanalyzer.pml.exporters.FileManager

import java.io.{File, FileOutputStream, FileWriter, PrintStream}

/**
  * Created by kdelmas on 28/04/16.
  */
object GnuPlotWriter {

  def writeDataPhase[T](
                         data: List[List[T]],
                         columnName: List[String],
                         title: String,
                         xlabel: String,
                         ylabel: String,
                         toTex: Boolean = false)(implicit ord: Ordering[T]): Unit = {
    val destinationFile = FileManager.analysisDirectory.getFile(title.replace(" ", "_").replace("\n", " ") + ".dat")
    val commandeFile = FileManager.analysisDirectory.getFile("gen" + title.replace(" ", "_").replace("\n", " ") + ".gnu")
    val outputFile = FileManager.analysisDirectory.getFile(title.replace(" ", "_").replace("\n", " ") + {
      if (toTex) ".tex" else ".png"
    })
    assert(data.forall(a => a.length == data.head.length))
    assert(columnName.size == data.head.length)
    val fos = new FileOutputStream(destinationFile)
    val writer = new PrintStream(fos)
    columnName.zipWithIndex.foreach(p => writer.println(s"#column${p._2} ${p._1}"))
    data.foreach(l => writer.println(l.mkString(" ")))
    writer.close()
    fos.close()
    val ymax = data.tail.map(l => l.max).max
    val ymin = data.tail.map(l => l.min).min
    val xmax = data.head.max
    val xmin = data.head.min
    val commandWriter = new FileWriter(commandeFile)
    commandWriter.write(
      s"""
         |reset
         |set term x11
         |set xlabel "$xlabel"
         |set ylabel "$ylabel"
         |set logscale y
         |set logscale x
         |unset key
         |set xrange [${List(xmin, ymin).min}:${List(xmax, ymax).max}]
         |set yrange [${List(xmin, ymin).min}:${List(xmax, ymax).max}]
         |plot x with lines title "x=y"
         |${(1 until data.head.length).map(i => s"replot ${"\"" + destinationFile.getAbsolutePath + "\""}  using 1:${i + 1} ").mkString("\n")}
         |set term ${if (toTex) "latex" else "png"}
         |set out "${outputFile.getAbsolutePath}"
         |rep
         |set out
         |set term x11
       """.stripMargin)
    commandWriter.close()
  }

  def printData[T](data: List[List[T]],
                   columnName: List[String],
                   title: String, xlabel: String,
                   ylabel: String,
                   toTex: Boolean = false,
                   drawXY: Boolean = true,
                   drawLines: Boolean = false,
                   logScale: Boolean = true): Unit = {
    val destinationFile = FileManager.analysisDirectory.getFile(title.replace(" ", "_").replace("\n", " ") + ".dat")
    val commandeFile = FileManager.analysisDirectory.getFile("gen" + title.replace(" ", "_").replace("\n", " ") + ".gnu")
    val outputFile = FileManager.analysisDirectory.getFile(title.replace(" ", "_").replace("\n", " ") + {
      if (toTex) ".tex" else ".png"
    })
    assert(data.forall(a => a.length == data.head.length))
    assert(columnName.size == data.head.length)
    val fos = new FileOutputStream(destinationFile)
    val writer = new PrintStream(fos)
    columnName.zipWithIndex.foreach(p => writer.println(s"#column${p._2} ${p._1}"))
    data.foreach(l => writer.println(l.mkString(" ")))
    writer.close()
    fos.close()
    val plotData =
      if (drawXY) {
        (1 until data.head.length).map(
          i => s"replot ${"\"" + destinationFile.getAbsolutePath + "\""} using 1:${i + 1}  ").mkString("\n")
      } else {
        (s"plot ${"\"" + destinationFile.getAbsolutePath + "\""} using 1:${2} ${if (drawLines) "with lines" else ""}" +:
          (2 until data.head.length).map(
            i => s"replot ${"\"" + destinationFile.getAbsolutePath + "\""} using 1:${i + 1} ${if (drawLines) "with lines" else ""} ")).mkString("\n")
      }
    val commandWriter = new FileWriter(commandeFile)
    commandWriter.write(
      s"""
         |reset
         |set term x11
         |set xlabel "$xlabel"
         |set ylabel "$ylabel"
         |${if (logScale) s"""set logscale y""" else ""}
         |${if (logScale) s"""set logscale x""" else ""}
         |unset key
         |${if (drawXY) s"""plot x with lines title "x=y" """ else ""}
         |${plotData}
         |set term ${if (toTex) "latex" else "png"}
         |set out "${outputFile.getAbsolutePath}"
         |rep
         |set out
         |set term x11
       """.stripMargin)
    commandWriter.close()
  }
}

