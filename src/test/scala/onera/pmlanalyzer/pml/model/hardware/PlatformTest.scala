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

import onera.pmlanalyzer.pml.model.service.*
import onera.pmlanalyzer.pml.model.software.*
import onera.pmlanalyzer.pml.model.hardware.*
import onera.pmlanalyzer.pml.operators._
import org.scalacheck.Gen
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

import scala.reflect.*

class PlatformTest
    extends AnyFlatSpec
    with ScalaCheckPropertyChecks
    with should.Matchers {

  override implicit val generatorDrivenConfig: PropertyCheckConfiguration =
    PropertyCheckConfiguration(minSuccessful = 200)

  object PlatformFixture
      extends Platform(Symbol("fixture"))
      with ApplicationTest
      with LoadTest
      with StoreTest
      with TargetTest
      with SimpleTransporterTest
      with SmartTest

  import PlatformFixture.*

  private def testBasics[T <: Hardware: Typeable](
      x: T,
      loads: List[Load],
      stores: List[Store]
  ) = {
    if (loads.nonEmpty)
      x.loads should be(loads.toSet)
    else
      x.loads should be(Set(Load(Symbol(s"${x}_load"))))
    if (stores.nonEmpty)
      x.stores should be(stores.toSet)
    else
      x.stores should be(Set(Store(Symbol(s"${x}_store"))))
  }

  "A platform" should "contains all applications" in {
    forAll { (x: Application) =>
      Application.all should contain(x)
    }
  }

  it should "retrieve composite properly" in {
    forAll("name") { (name: Symbol) =>
      whenever(Composite.all.forall(_.name != name)) {
        val CompositeTest = new Composite(name) {}
        // redundant ownership test but enforce object initialization
        CompositeTest.owner should be(currentOwner)
        Composite.all should contain(CompositeTest)
      }
    }
  }

  it should "retrieve smart services properly" in {
    forAll("name", "stores", "loads") {
      (name: Symbol, stores: List[Store], loads: List[Load]) =>
        {
          whenever(
            Initiator.all.forall(
              _.name != Initiator.formatName(name, currentOwner)
            )
          ) {
            val smart = Initiator(name, (loads ++ stores).toSet)
            testBasics(smart, loads, stores)
            Initiator.all should contain(smart)
          }
        }
    }
  }

  it should "retrieve target services properly" in {
    forAll("name", "stores", "loads") {
      (name: Symbol, stores: List[Store], loads: List[Load]) =>
        {
          whenever(
            Target.all.forall(_.name != Target.formatName(name, currentOwner))
          ) {
            val target = Target(name, (loads ++ stores).toSet)
            testBasics(target, loads, stores)
            Target.all should contain(target)
          }
        }
    }
  }

  it should "retrieve simple transporter services properly" in {
    forAll("name", "stores", "loads") {
      (name: Symbol, stores: List[Store], loads: List[Load]) =>
        {
          whenever(
            SimpleTransporter.all.forall(
              _.name != SimpleTransporter.formatName(name, currentOwner)
            )
          ) {
            val transporter = SimpleTransporter(name, (loads ++ stores).toSet)
            testBasics(transporter, loads, stores)
            SimpleTransporter.all should contain(transporter)
          }
        }
    }
  }

  it should "retrieve virtualizer services properly" in {
    forAll("name", "stores", "loads") {
      (name: Symbol, stores: List[Store], loads: List[Load]) =>
        {
          whenever(
            Virtualizer.all.forall(
              _.name != Virtualizer.formatName(name, currentOwner)
            )
          ) {
            val virtualizer = Virtualizer(name, (loads ++ stores).toSet)
            testBasics(virtualizer, loads, stores)
            Virtualizer.all should contain(virtualizer)
          }
        }
    }
  }

  it should "encode links and unlink properly" in {
    forAll(
      (Gen.oneOf(Initiator.all), "smart"),
      (Gen.oneOf(Target.all), "target"),
      (Gen.oneOf(SimpleTransporter.all), "transporter")
    ) { (smart, target, transporter) =>
      {
        smart link transporter
        transporter link target
        for (ss <- smart.stores)
          assert(transporter.stores.subsetOf(ss.linked))
        for (ss <- smart.loads)
          assert(transporter.loads.subsetOf(ss.linked))
        for (ss <- transporter.stores)
          assert(target.stores.subsetOf(ss.linked))
        for (ss <- transporter.loads)
          assert(target.loads.subsetOf(ss.linked))
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

  it should "encode deactivation properly" in {
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
