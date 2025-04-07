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
import scala.collection.mutable
import sourcecode.Name
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should

class TransitionTest extends AnyFlatSpec with should.Matchers {

  val p0: Place = Place("p0")
  val p1: Place = Place("p1")
  val t0: Transition = Transition(
    "t0",
    mutable.Map(p0 -> 1, p1 -> 2),
    mutable.Map(p0 -> 2, p1 -> 1)
  )
  val t1: Transition = Transition("t1", mutable.Map.empty, mutable.Map.empty)

  "A transition" should "have a name" in {
    t0.name shouldBe "t0"
  }
  it should "have a Pre and a Post map" in {
    t0.Pre(p0) shouldBe 1
    t0.Pre(p1) shouldBe 2
    t0.Post(p1) shouldBe 1
    t0.Post(p0) shouldBe 2
  }

  "A transition" should "be able to admit empty Pre or Post condition" in {
    t1.Pre.isEmpty shouldBe true
    t1.Post.isEmpty shouldBe true
  }
}
