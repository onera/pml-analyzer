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

import onera.pmlanalyzer.views.interference.model.formalisation.Petri.Place
import onera.pmlanalyzer.views.interference.model.formalisation.Petri.Transition
import onera.pmlanalyzer.views.interference.model.formalisation.Petri.Marking
import onera.pmlanalyzer.views.interference.model.formalisation.Petri.Marking.Marking
import onera.pmlanalyzer.views.interference.model.formalisation.Petri.PetriNetStruct
import onera.pmlanalyzer.views.interference.model.formalisation.Petri.PetriNet

import scala.collection.mutable
import sourcecode.Name
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should

class PetriNetTest extends AnyFlatSpec with should.Matchers {

  val p0: Place = Place("p0")
  val p1: Place = Place("p1")
  val p2: Place = Place("p2")
  val p3: Place = Place("p3")

  val t0: Transition = Transition(
    "t0",
    Marking(p0 -> 1, p1 -> 2),
    Marking(p0 -> 2, p1 -> 1)
  )
  val t1: Transition = Transition("t1", Marking.empty, Marking.empty)

  val s1: Marking = Marking(p0 -> 1)
  val s2: Marking = Marking(p0 -> 2, p1 -> 2)
  val s3: Marking = Marking.empty

  val struct = PetriNetStruct("S1", mutable.Set(p0, p1), mutable.Set(t0, t1))

  val noName: PetriNet = struct.withMarking(s1)
  val N1S1: PetriNet = struct.withMarking(s1, "N1S1")
  val N2S1: PetriNet = struct.withMarking(s2, "N2S1")
  val N2: PetriNet = PetriNetStruct.empty.withMarking(s2, "N2")

  "A PetriNet" should "have a name, a Place and a Transition Set" in {
    N1S1.name should be("N1S1")
    N2S1.name should be("N2S1")
    N2.name should be("N2")

    N1S1.places should contain(p0)
    N1S1.places should contain(p1)

    N1S1.transitions should contain(t0)
    N1S1.transitions should contain(t1)

    N2S1.places should contain(p0)
    N2S1.places should contain(p1)

    N2S1.transitions should contain(t0)
    N2S1.transitions should contain(t1)
  }

  it should "admit empty set of Places and Transitions" in {
    N2.places should be(empty)
    N2.transitions should be(empty)
  }

  it should "have an initial marking" in {
    N1S1.state should be(s1)
    N2.state should be(s2)
  }

  "Type Marking" should "have an empty element" in {
    s3 should be(empty)
  }

  "Changing structure" should "change related PetriNets" in {
    struct.addPlace(p2)
    N1S1.places should contain(p2)
    N2S1.places should contain(p2)
  }

  //FIXME Not very safe to change the structure from a PetriNet
  // the possibility to modify places and transitions after instantiation
  // is difficult to manage safely.
  // Perhaps consider immutable places and transitions after transformation into PetriNets.
  // Or use observed/observer design pattern to update PetriNet
  // To be discussed
  "Changing PetriNet" should "change structure (DANGEROUS)" in {
    N1S1.addPlace(p3)
    struct.places should contain (p3)
  }
}
