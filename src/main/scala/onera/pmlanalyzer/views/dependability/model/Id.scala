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

package onera.pmlanalyzer.views.dependability.model

import onera.pmlanalyzer.views.dependability.operators.IsFinite

import scala.collection.mutable

trait Id {
  val name: Symbol
  override def toString: String = name.name
}

trait IdBuilder[T <: Id] {
  val noneId: T
  val tyypeName: Symbol
  protected val _memo = mutable.HashMap.empty[Symbol, T]
  implicit object TIsFinite extends IsFinite[T] {
    val none: T = noneId
    override def typeName: Symbol = tyypeName
    def allWithNone: Seq[T] = _memo.values.toSeq :+ noneId
    def name(x: T): Symbol = x.name
  }
}

final case class VariableId private (name: Symbol) extends Id

object VariableId extends IdBuilder[VariableId] {
  val noneId = new VariableId(Symbol("none"))
  val tyypeName: Symbol = Symbol("VariableId")
  def apply(name: Symbol): VariableId =
    _memo.getOrElseUpdate(name, new VariableId(name))
}

class TargetId private (val name: Symbol) extends Id {
  override def toString: String = name.name
}

object TargetId extends IdBuilder[TargetId] {
  val noneId = new TargetId(Symbol("none"))
  val tyypeName: Symbol = Symbol("TargetId")
  def apply(name: Symbol): TargetId =
    _memo.getOrElseUpdate(name, new TargetId(name))
}

final case class SoftwareId private (name: Symbol) extends Id {
  override def toString: String = name.name
}

object SoftwareId extends IdBuilder[SoftwareId] {
  val noneId = new SoftwareId(Symbol("none"))
  val tyypeName: Symbol = Symbol("SoftwareId")
  def apply(name: Symbol): SoftwareId =
    _memo.getOrElseUpdate(name, new SoftwareId(name))
}

final case class TransporterId(name: Symbol) extends Id {
  override def toString: String = name.name
}

final case class InitiatorId private (name: Symbol) extends Id {
  override def toString: String = name.name
}

object InitiatorId extends IdBuilder[InitiatorId] {
  val noneId = new InitiatorId(Symbol("none"))
  val tyypeName: Symbol = Symbol("InitiatorId")
  def apply(name: Symbol): InitiatorId =
    _memo.getOrElseUpdate(name, new InitiatorId(name))
}

final case class AutomatonId(name: Symbol) extends Id
