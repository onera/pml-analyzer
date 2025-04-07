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

package onera.pmlanalyzer.views.interference.formalisation.Petri

import onera.pmlanalyzer.views.interference.model.formalisation.Petri.Place
import onera.pmlanalyzer.views.interference.model.formalisation.Petri.Transition
import onera.pmlanalyzer.views.interference.model.formalisation.Petri.Marking
import onera.pmlanalyzer.views.interference.model.formalisation.Petri.PetriNetStruct
import onera.pmlanalyzer.views.interference.model.formalisation.Petri.PetriNet
import scala.collection.mutable
import sourcecode.Name
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should

class PetriNetTest extends AnyFlatSpec with should.Matchers {

  val p0: Place = Place("p0")
  val p1: Place = Place("p1")
  val t0: Transition = Transition(
    "t0",
    mutable.Map(p0 -> 1, p1 -> 2),
    mutable.Map(p0 -> 2, p1 -> 1)
  )
  val t1: Transition = Transition("t1", mutable.Map.empty, mutable.Map.empty)

  val s1: Marking = mutable.Map(p0 -> 1)
  val s2: Marking = mutable.Map(p0 -> 2, p1 -> 2)
  val s3: Marking = mutable.Map.empty

  val N1: PetriNet =
    PetriNet("N1", mutable.Set(p0, p1), mutable.Set(t0, t1), s1)

  val N2: PetriNet =
    PetriNet("N2", mutable.Set.empty, mutable.Set.empty, s2)

  "A PetriNet" should "have a name, a Place and a Transition Set" in {
    N1.name should be("N1")
    N2.name should be("N2")

    N1.places should contain(p0)
    N1.places should contain(p1)

    N1.transitions should contain(t0)
    N1.transitions should contain(t1)
  }

  it should "admit empty set of Places and Transitions" in {
    N2.places should be(mutable.Set.empty)
    N2.transitions should be(mutable.Set.empty)
  }

  it should "have an initial marking" in {
    N1.state should be(s1)
    N2.state should be(s2)
  }

  "Type Marking" should "have an empty element" in {
    s3.isEmpty should be(true)
  }
}
