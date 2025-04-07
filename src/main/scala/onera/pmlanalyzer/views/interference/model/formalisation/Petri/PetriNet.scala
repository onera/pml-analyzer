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
import onera.pmlanalyzer.views.interference.model.formalisation.Petri.Transition
import onera.pmlanalyzer.views.interference.model.formalisation.Petri.PetriNetStruct

final class PetriNet(
    name: String,
    places: mutable.Set[Place],
    transitions: mutable.Set[Transition],
    initialMarking: Marking
) extends PetriNetStruct(
      name: String,
      places: mutable.Set[Place],
      transitions: mutable.Set[Transition]
    ) {

  val state = initialMarking

  def fireTransition(t: Transition) =
    if (transitions.contains(t) && t.enabled(state)) then
      for { (p, i) <- t.Pre } { state.getOrElseUpdate(p, (state(p) - i)) }
      for { (p, i) <- t.Post } { state.getOrElseUpdate(p, (state(p) + i)) }
    else println(s"Transition ${t} is not enabled.\n")

  def fireSequence(transSeq: List[Transition]): Unit =
    if (!transSeq.isEmpty) then
      if (transitions.contains(transSeq.head) && transSeq.head.enabled(state))
      then
        fireTransition(transSeq.head)
        fireSequence(transSeq.tail)
      else println(s"Transition ${transSeq.head} is not enabled.\n")

  override def toString() =
    s"Petri net ${name}:\nplaces: ${places}\ntransitions: ${transitions}\nmarking: ${state}\n"
}
