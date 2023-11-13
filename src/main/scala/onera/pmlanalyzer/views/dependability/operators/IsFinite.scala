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

package onera.pmlanalyzer.views.dependability.operators

trait IsFinite[T] {
  def typeName: Symbol = Symbol(all.map(fm => name(fm).name.charAt(0).toTitleCase).mkString(""))
  def allWithNone:Seq[T]
  def name(x:T):Symbol
  val none : T
  def all : Seq[T] =  allWithNone.filterNot(_ == none)
}

trait IsFiniteOps {

  implicit class hasName[T](x: T)(implicit ev: IsFinite[T]) {
    def name: Symbol = ev.name(x)
  }

  def nameOf[T](implicit ev: IsFinite[T]): Symbol = ev.typeName

  def allOf[T](implicit ev: IsFinite[T]): Seq[T] = ev.all

  def noneOf[T](implicit ev: IsFinite[T]): T = ev.none

  def allWithNone[T](implicit ev: IsFinite[T]): Seq[T] = ev.allWithNone
}

object IsFinite {
  implicit def tupleIsFinite[T,U] (implicit evT:IsFinite[T], evU:IsFinite[U]) : IsFinite[(T,U)] = new IsFinite[(T, U)] {
    override def typeName: Symbol = Symbol(s"Pair${evT.typeName.name}${evU.typeName.name}")
    def allWithNone: Seq[(T, U)] = evT.all.flatMap(t => evU.all.map(u => (t,u))) :+ none
    def name(x: (T, U)): Symbol = Symbol(s"${evT.name(x._1).name}_${evU.name(x._2).name}")
    val none: (T, U) = (evT.none, evU.none)
  }
}
