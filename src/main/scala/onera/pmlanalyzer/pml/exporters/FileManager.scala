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

package onera.pmlanalyzer.pml.exporters

import java.io.File
import scala.io.{BufferedSource, Source}
//import scala.reflect.io.Path

object FileManager {

  /**
    * Util case class encoding an output directory used by exporters and solvers
    * @param name the name of the directory stored at the project location
    */
  final case class OutputDirectory(name:String){
    private val directory = {
      val file = new File(name)
      if (file.exists())
        file
      else if (file.mkdir()) {
        file
      } else {
        throw new Exception(s"cannot create $file directory")
      }
    }

    /**
      * Create a new file in the current directory
      * @param s the file name
      * @return the java File
      */
    def getFile(s: String): File = {
      new File(directory, s)
    }

    /**
      * remove all the existing files of the directory
      */
    def clean(): Unit = ???

    /**
      * find recursively a file by its name
      * @param fileName name of the file to find
      * @return the java File if found
      */
    def locate(fileName: String) : Option[File] =
      OutputDirectory.recursiveLocateFirstFile(directory, (f: File) => f.getName == fileName)

  }

  object OutputDirectory {
    def recursiveLocateFirstFile(dir: File, filter: File => Boolean): Option[File] = {
      if (dir.exists()) {
        if (dir.isDirectory) {
          val these = dir.listFiles
          (for (r <- these.find(filter)) yield {
            r
          }) orElse {
            these.filter(_.isDirectory).flatMap(x => recursiveLocateFirstFile(x, filter)).toList match {
              case Nil => None
              case h :: _ => Some(h)
            }
          }
        } else {
          Some(dir).filter(filter)
        }
      } else
        None
    }
  }

  val analysisDirectory: OutputDirectory = OutputDirectory("analysis")

  val exportDirectory: OutputDirectory = OutputDirectory("export")

  val temporaryDirectory: OutputDirectory = OutputDirectory("temp")

  def extractResource(name:String) : Option[BufferedSource] = {
    val classLoader = getClass.getClassLoader
    val resource = Source.fromInputStream(classLoader.getResourceAsStream(name))
    Option(resource)
  }
}

