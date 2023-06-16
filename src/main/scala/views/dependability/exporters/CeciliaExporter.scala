/*******************************************************************************
 * Copyright (c) 2021. ONERA
 * This file is part of PML Analyzer
 *
 * PML Analyzer is free software ;
 * you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation ;
 * either version 2 of  the License, or (at your option) any later version.
 *
 * PML  Analyzer is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY ;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this program ;
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA
 ******************************************************************************/

package views.dependability.exporters

import pml.exporters.FileManager

import scala.xml.{Elem, XML}

trait CeciliaExporter[T] {
  type R

  def toCecilia(x: T): R
}

trait CeciliaExporterOps {
  implicit class ceciliaOps[T, O](x: T) {
    def toCecilia(implicit ev: CeciliaExporter.Aux[T, O]): O = ev.toCecilia(x)
    val ceciliaExportName : String = CeciliaExporter.ceciliaExportName(x)
    def exportAsCecilia(implicit ev: CeciliaExporter.Aux[T, O]) : Unit = {
      val file = FileManager.exportDirectory.getFile(ceciliaExportName+".xml")
      ev.toCecilia(x)
      val elem: Elem = {
        <cec.export>
          {PhylogFolder.allFamilies.map(_.toElem)}
        </cec.export>
      }
      XML.save(file.getAbsolutePath, elem)
    }
  }
}

object CeciliaExporter {

  type Aux[T, O] = CeciliaExporter[T] {
    type R = O
  }

  def ceciliaExportName[T](x:T): String = x match {
    case d: DependabilitySpecification => s"$x${d.depSpecificationName.name}"
    case _ => x.toString
  }
}
