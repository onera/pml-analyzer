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

package onera.pmlanalyzer.views.patterns.model

import scala.language.implicitConversions

private[pmlanalyzer] trait PatternAST {
  val label: String
  val implementation: Option[String]
  val textWidth: Option[Int]
}

private[pmlanalyzer] sealed trait Evidence extends PatternAST

private[pmlanalyzer] final case class Backing(
    label: String,
    implementation: Option[String] = None,
    textWidth: Option[Int] = None
) extends PatternAST

private[pmlanalyzer] final case class Defeater(
    label: String,
    implementation: Option[String] = None,
    textWidth: Option[Int] = None
) extends PatternAST

private[pmlanalyzer] final case class Strategy(
    label: String,
    backing: Option[Backing] = None,
    defeater: Option[Defeater] = None,
    implementation: Option[String] = None,
    textWidth: Option[Int] = None
) extends PatternAST {
  def backing(s: String): Strategy =
    copy(backing = Some(Backing(s)))
  def defeater(s: String): Strategy =
    copy(defeater = Some(Defeater(s)))
}

private[pmlanalyzer] final case class FinalEvidence(
    label: String,
    implementation: Option[String] = None,
    textWidth: Option[Int] = None,
    refOf: Option[Claim] = None
) extends Evidence

private[pmlanalyzer] final case class Given(
    label: String,
    implementation: Option[String] = None,
    textWidth: Option[Int] = None
) extends Evidence

private[pmlanalyzer] final case class Claim(
    label: String,
    short: Option[String],
    implementation: Option[String],
    textWidth: Option[Int],
    width: Option[Int],
    strategy: Strategy,
    evidences: Evidence*
) extends Evidence {
  def evidence(s: String): Claim =
    Claim(
      label,
      short,
      implementation,
      textWidth,
      width,
      strategy,
      evidences :+ FinalEvidence(s): _*
    )
  def evidence(b: Builder): Claim =
    Claim(
      label,
      short,
      implementation,
      textWidth,
      width,
      strategy,
      evidences :+ FinalEvidence(b.content, b.implementation, b.textWidth): _*
    )
  def `given`(s: String): Claim =
    Claim(
      label,
      short,
      implementation,
      textWidth,
      width,
      strategy,
      evidences :+ Given(s): _*
    )
  def `given`(b: Builder): Claim =
    Claim(
      label,
      short,
      implementation,
      textWidth,
      width,
      strategy,
      evidences :+ Given(b.content, b.implementation, b.textWidth): _*
    )
  def evidence(e: Evidence): Claim =
    Claim(
      label,
      short,
      implementation,
      textWidth,
      width,
      strategy,
      evidences :+ e: _*
    )
  def evidenceRef(claim: Claim): Claim =
    Claim(
      label,
      short,
      implementation,
      textWidth,
      width,
      strategy,
      evidences :+ FinalEvidence(claim.label, refOf = Some(claim)): _*
    )
  def allRef: Set[Claim] = evidences
    .collect {
      case c: Claim                          => c.allRef
      case FinalEvidence(_, _, _, Some(ref)) => Set(ref)
    }
    .toSet
    .flatten
}

private[pmlanalyzer] object Claim {

  def computeIdIn(c: Claim): Map[PatternAST, String] = {

    val strategyIdByLevel =
      collection.mutable.HashMap.empty[Int, Int].withDefaultValue(0)
    val evidenceIdByLevel =
      collection.mutable.HashMap.empty[Int, Int].withDefaultValue(0)
    val givenIdByLevel =
      collection.mutable.HashMap.empty[Int, Int].withDefaultValue(0)

    def getFreshId(
        level: Int,
        ids: collection.mutable.Map[Int, Int]
    ): () => Int = {
      ids(level) = ids(level) + 1
      val x = ids(level)
      () => (0 until level).map(i => ids(i)).sum + x
    }

    def byLevel(
        claim: Claim,
        currentLevel: Int
    ): Map[PatternAST, () => String] = {
      val cId = (for { s <- claim.short if currentLevel == 0 } yield () =>
        s"($s)") getOrElse {
        val x = getFreshId(currentLevel, evidenceIdByLevel)
        () => s"(E${x()})"
      }
      val sId = {
        val x = getFreshId(currentLevel, strategyIdByLevel)
        () => s"(W${x()})"
      }
      claim.evidences.flatMap {
        case f: FinalEvidence =>
          val x = getFreshId(currentLevel + 1, evidenceIdByLevel)
          Set(f -> (() => s"(E${x()})"))
        case g: Given =>
          val x = getFreshId(currentLevel + 1, givenIdByLevel)
          Set(g -> (() => s"(G${x()})"))
        case c: Claim =>
          byLevel(c, currentLevel + 1)
      }.toMap + (claim -> cId) + (claim.strategy -> sId)
    }
    byLevel(c, 0).transform((_, v) => v())
  }
}

private[pmlanalyzer] final case class Builder(
    content: String,
    short: Option[String],
    implementation: Option[String],
    textWidth: Option[Int],
    width: Option[Int]
) {
  def short(s: String): Builder =
    Builder(content, Some(s), implementation, textWidth, width)
  def size(s: Int): Builder =
    Builder(content, short, implementation, Some(s), width)
  def width(s: Int): Builder =
    Builder(content, short, implementation, textWidth, Some(s))
  def strategy(s: Strategy): Claim =
    Claim(content, short, implementation, textWidth, width, s)
  def strategy(b: Builder): Claim =
    Claim(
      content,
      short,
      implementation,
      textWidth,
      width,
      Strategy(b.content, None, None, implementation, b.textWidth)
    )
  def backing(s: String): Strategy =
    Strategy(content, Some(Backing(s)), None, implementation, textWidth)
  def defeater(s: String): Strategy =
    Strategy(content, None, Some(Defeater(s)), implementation, textWidth)
  def implementation(i: String): Builder =
    copy(implementation = Some(s"\\textcolor{red}{$i}"))
}

private[pmlanalyzer] object PatternAST {

  trait Ops {
    implicit def toBuilder(s: String): Builder =
      Builder(s, None, None, None, None)

    def conclusion(s: Builder): Builder = s
  }
}
