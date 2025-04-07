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

package onera.pmlanalyzer.views.interference.model.formalisation.Petri

import scala.collection.mutable
import onera.pmlanalyzer.views.interference.model.formalisation.Petri.Place

/* Inductive type mapping transitions to their platform interpretation */
// enum TransitionType:
//   case ServiceP
//   case ApplicationP
//   case ExclusiveP
//   case CtrlP
//   case TransINOutP
//   case CheckInOutP
type Marking = mutable.Map[Place, Int]

def >=(x: Marking, y: Marking): Boolean =
  if (x.keySet ++ y.keySet == x.keySet) {
    val compare = for {
      k <- y.keySet
    } yield x(k) < y(k)
    if (compare.size >= 1) then false else true
  } else false

def isEmpty(m: Marking) = (m == mutable.Map.empty)

/** Class defining places 
  * @param name
  *  The name of the place
  * @param Pre
  *   The precondition modeling the transition's demand
  * @param Post
  *   The postcondition modeliong the transition's effects
  */
case class Transition(
    name: String,
    Pre: Marking,
    Post: Marking
) {

  /* Function that add a weighted arc from a place to a transition */
  def add_place_to_pre(p: Place, weight: Int) = Pre.getOrElseUpdate(p, weight)

  /* Function that add a weighted arc from a transition to a place */
  def add_place_to_post(p: Place, weight: Int) = Post.getOrElseUpdate(p, weight)

  /* Function that returns whether or not a transition is enabled in a marking */
  def enabled(m: Marking): Boolean = >=(m, this.Pre)

  override def toString(): String = name

}
