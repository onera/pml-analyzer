/*******************************************************************************
 * Copyright (c)  2021. ONERA
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

package onera.pmlanalyzer.views.dependability.model

import onera.pmlanalyzer.views.dependability.exporters.SubComponent

import scala.collection.mutable

trait Builder[T]{
  val toBuild:  mutable.HashMap[Symbol, () => T] = mutable.HashMap.empty
}

trait Linker{
  val links : mutable.HashMap[Variable[_], Set[Variable[_]]] = mutable.HashMap.empty
}

trait Owner {
  val portOwner : mutable.HashMap[VariableId, Component] = mutable.HashMap.empty
  val componentOwner :  mutable.HashMap[Component, Component] = mutable.HashMap.empty
}

case class System(name:Symbol) {

  implicit val context: Builder[SubComponent] with Linker with Owner = new Builder[SubComponent] with Linker with Owner {}

  override def toString: String = name.name

  implicit class listExtensionMethods[T](l:List[OutputPort[T]]) {
    def |+|(that : OutputPort[T]) : List[OutputPort[T]] =   l :+ that
    def |++|(that : List[OutputPort[T]]) : List[OutputPort[T]] =  l ++ that
  }
}

