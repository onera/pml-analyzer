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
import onera.pmlanalyzer.views.interference.model.formalisation.Petri.Marking
import onera.pmlanalyzer.views.interference.model.formalisation.Petri.Marking.Marking

/**
  * Trait defining the structure of a Petri net (without the marking)
  *
  * @param name
  *   The name of the associated Platform 
  * @param places
  *   The set of places
  * @param transitions
  *   The set of transitions
  */
class PetriNetStruct(
    val name: String,
    val places: mutable.Set[Place],
    val transitions: mutable.Set[Transition]
) {
  /**
   * Function that add a place to a PetriNetSturct
   * @param p
   * @return
   */
  def addPlace(p: Place): Unit =
    places += p

  /**
   * Function that add a transition to a PetriNetSturct
   * @param t
   * @return
   */
  def addTransition(t: Transition): Unit =
    transitions += t

  /**
   *
   * @param initialMarking
   * @param newName
   * @return
   */
  def withMarking(initialMarking: Marking, newName:String = name): PetriNet =
    PetriNet(newName, this, initialMarking)
}

object PetriNetStruct {
  val empty: PetriNetStruct = PetriNetStruct("empty",mutable.Set.empty, mutable.Set.empty)
}
