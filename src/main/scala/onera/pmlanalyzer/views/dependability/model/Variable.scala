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

import onera.pmlanalyzer.pml.model.hardware.Target as PMLTarget
import onera.pmlanalyzer.pml.model.software.Data
import onera.pmlanalyzer.{Target => PMLTarget, *}
import onera.pmlanalyzer.pml.operators.Used
import onera.pmlanalyzer.views.dependability.operators.{
  IsCriticalityOrdering,
  IsFinite
}
import scalaz.Leibniz

import scala.language.implicitConversions

sealed trait Expr[+T] {
  def eval(): Option[T]
}

private[pmlanalyzer] object Expr {

  trait Ops {
    implicit def toConst[T](x: T): Const[T] = Const(x)

    implicit def pairToDMap[T](m: (TargetId, Expr[T])): DMap[T] = DMap(Map(m))

    implicit def pairSeqToDMap[T](m: Seq[(TargetId, Expr[T])]): DMap[T] = DMap(
      Map(m: _*)
    )

    extension [T](m: Variable[Map[TargetId, T]]) {
      def fmOf(id: TargetId): Of[T] = Of(m, id)
      def fmOf(t: Target[T]): Of[T] = Of(m, t.id)
      def fmOf(
          d: Data
      )(implicit ev: PMLTarget => TargetId, u: Used[Data, PMLTarget]): Of[T] =
        Of(
          m,
          ev(d.hostingTargets.head)
        ) // FIXME ERROR IF SEVERAL TARGET FOR DATA
    }
    extension (e: Expr[_]) {
      def ===(that: Expr[_]): Equal = Equal(e, that)
    }

    extension (e: BoolExpr) {
      def and(that: BoolExpr): And = And(e, that)
      def or(that: BoolExpr): Or = Or(e, that)
    }

    extension (d: Data) {
      def fmIs[T](v: Expr[T])(implicit
          ev: PMLTarget => TargetId,
          u: Used[Data, PMLTarget]
      ): (TargetId, Expr[T]) =
        ev(d.hostingTargets.head) -> v
    }

    extension (d: PMLTarget) {
      def fmIs[T](v: Expr[T])(implicit
          ev: PMLTarget => TargetId
      ): (TargetId, Expr[T]) = ev(d) -> v
      def id(implicit ev: PMLTarget => TargetId): TargetId = ev(d)
    }

    extension (d: TargetId) {
      def fmIs[T](v: Expr[T]): (TargetId, Expr[T]) = d -> v
    }

    extension [T](d: Target[T]) {
      def fmIs(v: Expr[T]): (TargetId, Expr[T]) = d.id -> v
    }

    final case class If[T](b: BoolExpr) {
      def Then(t: Expr[T]): IT[T] = IT(b, t)
    }

    final case class IT[T](b: BoolExpr, t: Expr[T]) {
      def Else[U >: T](e: Expr[U]): ITE[U] = ITE(b, t, e)
    }
  }
}

private[pmlanalyzer] final case class ITE[T](
    i: BoolExpr,
    t: Expr[T],
    e: Expr[T]
) extends Expr[T] {
  def eval(): Option[T] = for (vi <- i.eval(); vt <- t.eval(); ve <- e.eval())
    yield if (vi) vt else ve
}

private[pmlanalyzer] final case class Const[T](x: T) extends Expr[T] {
  def eval(): Some[T] = Some(x)
}

private[pmlanalyzer] final case class DMap[T](m: Map[TargetId, Expr[T]])
    extends Expr[Map[TargetId, T]] {
  def eval(): Option[Map[TargetId, T]] = {
    val values = m.transform((_, v) => v.eval())
    if (values.values.exists(_.isEmpty))
      None
    else
      Some(values.collect({ case (k, Some(v)) => k -> v }))
  }
}

private[pmlanalyzer] final case class Of[T](
    m: Variable[Map[TargetId, T]],
    id: TargetId
) extends Expr[T] {
  def eval(): Option[T] = for { mv <- m.eval(); vv <- mv.get(id) } yield vv
}

private[pmlanalyzer] final case class Worst[T](l: Expr[T]*)(implicit
    ev: IsCriticalityOrdering[T],
    evF: IsFinite[T]
) extends Expr[T] {
  val ordering: IsCriticalityOrdering[T] = ev
  val finite: IsFinite[T] = evF
  def eval(): Option[T] = {
    val values = for { e <- l; ve <- e.eval() } yield ve
    if (values.size != l.size)
      None
    else
      Some(values.max)
  }
}

private[pmlanalyzer] final case class Best[T](l: Expr[T]*)(implicit
    evO: IsCriticalityOrdering[T],
    evF: IsFinite[T]
) extends Expr[T] {
  val ordering: IsCriticalityOrdering[T] = evO
  val finite: IsFinite[T] = evF
  def eval(): Option[T] = {
    val values = for { e <- l; ve <- e.eval() } yield ve
    if (values.size != l.size)
      None
    else
      Some(values.min)
  }
}

sealed trait BoolExpr extends Expr[Boolean]

private[pmlanalyzer] final case class Equal(l: Expr[_], r: Expr[_])
    extends BoolExpr {
  def eval(): Option[Boolean] = for {
    vl <- l.eval(); vr <- r.eval()
  } yield vr == vl
}

private[pmlanalyzer] final case class And(l: BoolExpr*) extends BoolExpr {
  override def eval(): Option[Boolean] = {
    val and = for { e <- l; ve <- e.eval() } yield ve
    if (and.size != l.size)
      None
    else
      Some(and.forall(x => x))
  }
}

private[pmlanalyzer] final case class Or(l: BoolExpr*) extends BoolExpr {
  override def eval(): Option[Boolean] = {
    val or = for { e <- l; ve <- e.eval() } yield ve
    if (or.size != l.size)
      None
    else
      Some(or.exists(x => x))
  }
}

private[pmlanalyzer] final case class Not(n: BoolExpr) extends BoolExpr {
  override def eval(): Option[Boolean] =
    for { vn <- n.eval() } yield !vn
}

sealed trait Variable[T] extends Expr[T] {

  val id: VariableId

  def eval(): Option[T]

  override def toString: String = id.name.name
}

private[pmlanalyzer] object Variable {

//  implicit def defOutPort[T](f: => Option[T]) : OutputPort[T] = OutputPort(() => f)
//
  implicit def toOptionList[T](o: OutputPort[T]): OutputPort[List[T]] =
    OutputPort(o.id, () => for (i <- o.eval()) yield List(i))
//
//  implicit def defLocalVar[T](f: => Option[T]) : LocalVariable[T] = LocalVariable(() => f)
}

private[pmlanalyzer] final case class OutputPort[T](
    id: VariableId,
    evalFun: () => Option[T]
) extends Variable[T] {
  def eval(): Option[T] = for (x <- evalFun()) yield x
  def |+|(that: OutputPort[T]): List[OutputPort[T]] = this :: that :: Nil
}

private[pmlanalyzer] final case class LocalVariable[T](
    id: VariableId,
    evalFun: () => Option[T]
) extends Variable[T] {
  def eval(): Option[T] = for (x <- evalFun()) yield x
}

private[pmlanalyzer] class InputPort[T](val id: VariableId)
    extends Variable[T] {

  private var evalFun: () => Option[T] = () => None

  def :=[U](
      other: List[OutputPort[U]]
  )(implicit linker: Linker, ev: Leibniz[Nothing, Any, List[U], T]): Unit = {
    linker.links.get(this) match {
      case Some(l) => linker.links(this) = l ++ other
      case None    => linker.links(this) = other.toSet
    }
    evalFun = () => Some(ev(other.flatMap(_.evalFun())))
  }

  def :=(other: OutputPort[T])(implicit linker: Linker): Unit = {
    linker.links.get(this) match {
      case Some(l) => linker.links(this) = l + other
      case None    => linker.links(this) = Set(other)
    }
    evalFun = () => other.evalFun()
  }

  def :=(other: InputPort[T])(implicit linker: Linker): Unit = {
    linker.links.get(this) match {
      case Some(l) => linker.links(this) = l + other
      case None    => linker.links(this) = Set(other)
    }
    evalFun = () => other.evalFun()
  }

  def :=(f: => Option[T]): Unit = {
    evalFun = () => f
  }

  def eval(): Option[T] = for (x <- evalFun()) yield x
}

private[pmlanalyzer] object InputPort {
  def apply[T](name: Symbol): InputPort[T] = new InputPort(VariableId(name))
}
