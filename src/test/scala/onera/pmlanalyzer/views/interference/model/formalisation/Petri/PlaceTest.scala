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
import sourcecode.Name
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should
import org.scalacheck.{Arbitrary, Gen}

trait PlaceGenTest {
  implicit val genPlace: Gen[Place] =
    for {
      name <- Gen.identifier
    } yield Place(name)
}

class PlaceTest extends AnyFlatSpec with PlaceGenTest with should.Matchers {

  // TODO provide a generator of Places for tests

  val p1: Place = Place("p1")

  "A place" should "have a name" in {
    p1.name should be("p1")
  }
}
