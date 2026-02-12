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

import onera.pmlanalyzer.views.interference.model.formalisation.Petri.Marking.*

import scala.collection.mutable
import onera.pmlanalyzer.views.interference.model.formalisation.Petri.Place

/** Class defining places
  * @param name
  *  The name of the place
  * @param pre
  *   The precondition modeling the transition's demand
  * @param post
  *   The postcondition modeliong the transition's effects
  */
final case class Transition(
    name: String,
    pre: Marking,
    post: Marking
) {

  /**
   * Function that add a weighted arc from a place to a transition
   * @param p
   * @param weight
   * @return
   */
  def addPlaceToPre(p: Place, weight: Int) = pre.getOrElseUpdate(p, weight)

  /**
   * Function that add a weighted arc from a transition to a place
   * @param p
   * @param weight
   * @return
   */
  def addPlaceToPost(p: Place, weight: Int) = post.getOrElseUpdate(p, weight)

  /**
   *  Function that returns whether or not a transition is enabled in a marking
   * @param m
   * @return
   */
  def enabled(m: Marking): Boolean = m >= this.pre

  override def toString: String = name
}
