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

import onera.pmlanalyzer.views.interference.model.formalisation.Petri.Marking.Marking

import scala.collection.mutable
import onera.pmlanalyzer.views.interference.model.formalisation.Petri.Place
import onera.pmlanalyzer.views.interference.model.formalisation.Petri.Transition
import onera.pmlanalyzer.views.interference.model.formalisation.Petri.PetriNetStruct

import scala.annotation.tailrec

final class PetriNet private (
    name: String,
    places: mutable.Set[Place],
    transitions: mutable.Set[Transition],
    initialMarking: Marking
) extends PetriNetStruct(name, places, transitions) {

  val state: Marking = initialMarking

  def fireTransition(t: Transition): Unit =
    if (transitions.contains(t) && t.enabled(state)) then
      for { (p, i) <- t.pre } { state.getOrElseUpdate(p, state(p) - i) }
      for { (p, i) <- t.post } { state.getOrElseUpdate(p, state(p) + i) }
    else println(s"Transition $t is not enabled.\n")

  def fireSequence(transSeq: Seq[Transition]): Unit =
    for {
      t <- transSeq
      if transitions.contains(t)
    }
      if (t.enabled(state))
        fireTransition(t)
      else
        println(s"Transition $t is not enabled.")

  override def toString: String =
    s"Petri net $name:\nplaces: $places\ntransitions: $transitions\nmarking: $state\n"
}

object PetriNet {
  def apply(
      name: String,
      struct: PetriNetStruct,
      initialMarking: Marking
  ): PetriNet =
    new PetriNet(name, struct.places, struct.transitions, initialMarking)
}
