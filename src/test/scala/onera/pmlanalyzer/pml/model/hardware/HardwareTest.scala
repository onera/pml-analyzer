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

package onera.pmlanalyzer.pml.model.hardware

import onera.pmlanalyzer.pml.model.hardware.*
import onera.pmlanalyzer.pml.model.service.{Load, Store}
import onera.pmlanalyzer.pml.operators.*
import sourcecode.Name
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should

class HardwareTest extends AnyFlatSpec with should.Matchers {

  /* Create a default platform to act as a container for components. */
  object PlatformFixture extends Platform(Symbol("fixture"))

  import PlatformFixture.*

  "A Hardware" should "have default services" in {
    val t: Target = Target()
    val s: SimpleTransporter = SimpleTransporter()
    val i: Initiator = Initiator()
    val v: Virtualizer = Virtualizer()

    for (h <- List(t, s, i, v)) {
      exactly(1, h.services) shouldBe a[Load]
      exactly(1, h.services) shouldBe a[Store]
      h.services.size shouldBe 2
    }
  }

  it should "have no services when specified" in {
    val t: Target = Target(withDefaultServices = false)
    val s: SimpleTransporter = SimpleTransporter(withDefaultServices = false)
    val i: Initiator = Initiator(withDefaultServices = false)
    val v: Virtualizer = Virtualizer(withDefaultServices = false)

    for (h <- List(t, s, i, v)) {
      h.services shouldBe empty
    }
  }

  it should "have only specified services when specified" in {
    val t: Target =
      Target(Set(Load("a"), Load("b")), withDefaultServices = false)
    t.services.size shouldEqual 2
    exactly(2, t.services) shouldBe a[Load]

    val s = Target(Set(Store("a")), withDefaultServices = false)
    s.services.size shouldEqual 1
    exactly(1, s.services) shouldBe a[Store]

    val i = Target(Set.empty, withDefaultServices = false)
    i.services.size shouldEqual 0

    val j = Target(Symbol("j"), withDefaultServices = false)
    j.services.size shouldEqual 0
  }

}
