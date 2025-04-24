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

import onera.pmlanalyzer.pml.model.PMLNodeBuilder
import onera.pmlanalyzer.pml.model.service.*
import onera.pmlanalyzer.pml.model.software.*
import onera.pmlanalyzer.pml.model.hardware.*
import onera.pmlanalyzer.pml.operators.*
import org.scalacheck.{Arbitrary, Gen}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

import scala.reflect.*
import onera.pmlanalyzer.views.interference.InterferenceTestExtension.UnitTests

class PlatformTest
    extends AnyFlatSpec
    with ScalaCheckPropertyChecks
    with should.Matchers {

  override implicit val generatorDrivenConfig: PropertyCheckConfiguration =
    PropertyCheckConfiguration(minSuccessful = 200)

  object PlatformTestFixture
      extends Platform(Symbol("PlatformTestFixture"))
      with ApplicationArbitrary
      with LoadArbitrary
      with StoreArbitrary
      with TargetArbitrary
      with SimpleTransporterArbitrary
      with InitiatorArbitrary

  import PlatformTestFixture.*
  import PlatformTestFixture.given

  private def testBasics[T <: Hardware: Typeable](
      x: T,
      loads: List[Load],
      stores: List[Store]
  ) = {
    if (loads.nonEmpty)
      x.loads should be(loads.toSet)
    else
      x.loads.map(_.name) should be(Set(Symbol(s"${x}_load")))
    if (stores.nonEmpty)
      x.stores should be(stores.toSet)
    else
      x.stores.map(_.name) should be(Set(Symbol(s"${x}_store")))
  }

  "A platform" should "contains all applications" taggedAs UnitTests in {
    forAll { (x: Application) =>
      Application.all should contain(x)
    }
  }

  it should "retrieve composite properly" taggedAs UnitTests in {
    forAll("name") { (name: Symbol) =>
      whenever(
        Composite.get(Composite.formatName(name, currentOwner)).isEmpty
      ) {
        val CompositeTest = new Composite(name) {}
        // redundant ownership test but enforce object initialization
        CompositeTest.owner should be(currentOwner)
        Composite.all should contain(CompositeTest)
      }
    }
  }

  it should "retrieve smart services properly" taggedAs UnitTests in {
    forAll("name", "stores", "loads") {
      (name: Symbol, stores: List[Store], loads: List[Load]) =>
        {
          whenever(
            Initiator.get(PMLNodeBuilder.formatName(name, currentOwner)).isEmpty
              && stores.nonEmpty
              && loads.nonEmpty
          ) {
            val smart = Initiator(name, (loads ++ stores).toSet)
            testBasics(smart, loads, stores)
            Initiator.all should contain(smart)
          }
        }
    }
  }

  it should "retrieve target services properly" taggedAs UnitTests in {
    forAll("name", "stores", "loads") {
      (name: Symbol, stores: List[Store], loads: List[Load]) =>
        {
          whenever(
            Target.get(PMLNodeBuilder.formatName(name, currentOwner)).isEmpty
              && stores.nonEmpty
              && loads.nonEmpty
          ) {
            val target = Target(name, (loads ++ stores).toSet)
            testBasics(target, loads, stores)
            Target.all should contain(target)
          }
        }
    }
  }

  it should "retrieve simple transporter services properly" taggedAs UnitTests in {
    forAll("name", "stores", "loads") {
      (name: Symbol, stores: List[Store], loads: List[Load]) =>
        {
          whenever(
            SimpleTransporter
              .get(PMLNodeBuilder.formatName(name, currentOwner))
              .isEmpty
              && stores.nonEmpty
              && loads.nonEmpty
          ) {
            val transporter = SimpleTransporter(name, (loads ++ stores).toSet)
            testBasics(transporter, loads, stores)
            SimpleTransporter.all should contain(transporter)
          }
        }
    }
  }

  it should "retrieve virtualizer services properly" taggedAs UnitTests in {
    forAll("name", "stores", "loads") {
      (name: Symbol, stores: List[Store], loads: List[Load]) =>
        {
          whenever(
            Virtualizer
              .get(PMLNodeBuilder.formatName(name, currentOwner))
              .isEmpty
              && stores.nonEmpty
              && loads.nonEmpty
          ) {
            val virtualizer = Virtualizer(name, (loads ++ stores).toSet)
            testBasics(virtualizer, loads, stores)
            Virtualizer.all should contain(virtualizer)
          }
        }
    }
  }

  it should "encode links and unlink properly" taggedAs UnitTests in {
    forAll(
      (Gen.oneOf(Initiator.all), "smart"),
      (Gen.oneOf(Target.all), "target"),
      (Gen.oneOf(SimpleTransporter.all), "transporter")
    ) { (smart, target, transporter) =>
      {
        smart link transporter
        transporter link target
        for {
          ss <- smart.stores
          ts <- transporter.stores
        }
          ss.linked should contain(ts)
        for {
          sl <- smart.loads
          tl <- transporter.loads
        }
          sl.linked should contain(tl)
        for {
          trs <- transporter.stores
          ts <- target.stores
        }
          trs.linked should contain(ts)
        for {
          trl <- transporter.loads
          tl <- target.loads
        }
          trl.linked should contain(tl)
        smart unlink transporter
        for (ss <- smart.stores)
          ss.linked.intersect(transporter.stores) should be(empty)
        for (ss <- smart.loads)
          ss.linked.intersect(transporter.loads) should be(empty)
        transporter unlink target
        for (ss <- transporter.stores)
          ss.linked.intersect(target.stores) should be(empty)
        for (ss <- transporter.loads)
          ss.linked.intersect(target.loads) should be(empty)
      }
    }
  }

  it should "encode deactivation properly" taggedAs UnitTests in {
    forAll(
      (Gen.oneOf(Initiator.all), "smart"),
      (Gen.oneOf(Target.all), "target"),
      (Gen.oneOf(SimpleTransporter.all), "transporter")
    ) { (smart, target, transporter) =>
      {
        smart.deactivated
        smart.services should be(empty)
        transporter.deactivated
        transporter.services should be(empty)
        target.deactivated
        target.services should be(empty)
      }
    }
  }
}
