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

package onera.pmlanalyzer.views.dependability.exporters

import onera.pmlanalyzer.*
import onera.pmlanalyzer.views.dependability.exporters.CeciliaExporter.Aux
import onera.pmlanalyzer.views.dependability.model.*

private[pmlanalyzer] trait ExprCeciliaExporter {
  self: BasicOperationCeciliaExporter with TypeCeciliaExporter =>

  final case class AssertionHelper(
      result: Map[TargetId, String],
      subComponentAssertions: Map[SubComponent, Seq[String]]
  )

  implicit def ExprCeciliaExporter[T]: Aux[Expr[T], AssertionHelper] =
    new CeciliaExporter[Expr[T]] {
      type R = AssertionHelper

      def boolExprToHelper(x: BoolExpr): AssertionHelper = x match {
        case Equal(l, r) =>
          val lMap = toCecilia(l.asInstanceOf[Expr[T]])
          val rMap = toCecilia(r.asInstanceOf[Expr[T]])
          val keys = lMap.result.keySet.intersect(rMap.result.keySet)
          AssertionHelper(
            keys.map(k => k -> s"${lMap.result(k)} = ${rMap.result(k)}").toMap,
            lMap.subComponentAssertions ++ rMap.subComponentAssertions
          )
        case And(l*) =>
          val lR = l.map(boolExprToHelper)
          val keys = lR.foldLeft(allOf[TargetId].toSet)((acc, m) =>
            acc.intersect(m.result.keySet)
          )
          AssertionHelper(
            keys.map(k => k -> lR.map(_.result(k)).mkString(" and ")).toMap,
            lR.map(_.subComponentAssertions).reduce(_ ++ _)
          )

        case Or(l*) =>
          val lR = l.map(boolExprToHelper)
          val keys = lR.foldLeft(allOf[TargetId].toSet)((acc, m) =>
            acc.intersect(m.result.keySet)
          )
          AssertionHelper(
            keys.map(k => k -> lR.map(_.result(k)).mkString(" or ")).toMap,
            lR.map(_.subComponentAssertions).reduce(_ ++ _)
          )
        case Not(n) =>
          val r = boolExprToHelper(n)
          AssertionHelper(
            r.result.transform((_, v) => s"not $v"),
            r.subComponentAssertions
          )
      }

      def toCecilia(x: Expr[T]): AssertionHelper = x match {
        case ITE(i, t, e) =>
          val tMap = toCecilia(t)
          val eMap = toCecilia(e)
          val iMap = boolExprToHelper(i)
          val keys = tMap.result.keySet
            .intersect(eMap.result.keySet)
            .intersect(iMap.result.keySet)
          val iteMap = keys
            .map(k =>
              k -> {
                s"""( if (${iMap.result(k)}) then
             |    ${tMap.result(k)}
             |  else
             |    ${eMap.result(k)}
             |)""".stripMargin
              }
            )
            .toMap
          AssertionHelper(
            iteMap,
            tMap.subComponentAssertions ++ eMap.subComponentAssertions ++ iMap.subComponentAssertions
          )
        case Const(x) =>
          AssertionHelper(
            allOf[TargetId].map(t => t -> x.toString).toMap,
            Map.empty
          )
        case DMap(p: Map[TargetId, Expr[_]]) =>
          val r = p.transform((_, v) => toCecilia(v.asInstanceOf[Expr[T]]))
          AssertionHelper(
            r.transform((k, v) => v.result(k)),
            r.map(_._2.subComponentAssertions).reduce(_ ++ _)
          )
        case Of(m, id) =>
          AssertionHelper(
            allOf[TargetId].map(t => t -> s"$m^$id").toMap,
            Map.empty
          )
        case Worst(l*) if l.size == 1 =>
          toCecilia(l.head)
        case w @ Worst(l*) =>
          val lR = l.map(toCecilia)
          val lMap =
            lR.foldLeft(Map.empty[TargetId, List[String]])((acc, m) => {
              m.result.transform((k, v) =>
                (for { a <- acc.get(k) } yield a :+ v) getOrElse List(v)
              )
            })
          val worsts =
            lMap.transform((k, v) => mkWorstSub(v.toSet)(w.finite, w.ordering))
          AssertionHelper(
            worsts.transform((_, v) => v._1),
            lR.map(_.subComponentAssertions).reduce(_ ++ _) ++ worsts.map(p =>
              p._2._3 -> p._2._2
            )
          )
        case Best(l*) if l.size == 1 =>
          toCecilia(l.head)
        case b @ Best(l*) =>
          val lR = l.map(toCecilia)
          val lMap =
            lR.foldLeft(Map.empty[TargetId, List[String]])((acc, m) => {
              m.result.transform((k, v) =>
                (for { a <- acc.get(k) } yield a :+ v) getOrElse List(v)
              )
            })
          val bests =
            lMap.transform((k, v) => mkWorstSub(v.toSet)(b.finite, b.ordering))
          AssertionHelper(
            bests.transform((_, v) => v._1),
            lR.map(_.subComponentAssertions).reduce(_ ++ _) ++ bests.map(p =>
              p._2._3 -> p._2._2
            )
          )
        case v: Variable[_] =>
          AssertionHelper(
            allOf[TargetId].map(t => t -> v.toString).toMap,
            Map.empty
          )
        case b: BoolExpr => boolExprToHelper(b)
      }

    }
}
