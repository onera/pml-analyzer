package onera.pmlanalyzer.pml.model.hardware

import onera.pmlanalyzer.pml.model.hardware.*
import onera.pmlanalyzer.pml.model.service.{Load, Store}
import onera.pmlanalyzer.pml.model.utils.{Owner, ReflexiveInfo}
import onera.pmlanalyzer.pml.operators.*
import sourcecode.{File, Line, Name}
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
      h.services.size should be(2)
    }
  }

  it should "have no services when specified" in {
    val t: Target = Target(withDefaultServices = false)
    val s: SimpleTransporter = SimpleTransporter(withDefaultServices = false)
    val i: Initiator = Initiator(withDefaultServices = false)
    val v: Virtualizer = Virtualizer(withDefaultServices = false)

    for (h <- List(t, s, i, v)) {
      h.services should be(empty)
    }
  }

  it should "have only specified services when specified" in {
    val t: Target =
      Target(Set(Load("a"), Load("b")), withDefaultServices = false)
    t.services.size should be(2)
    exactly(2, t.services) should be(a[Load])

    val s = Target(Set(Store("a")), withDefaultServices = false)
    s.services.size should be(1)
    exactly(1, s.services) should be(a[Store])

    val i = Target(Set.empty, withDefaultServices = false)
    i.services.size should be(0)

    val j = Target(Symbol("j"), withDefaultServices = false)
    j.services.size should be(0)
  }

  it should "raise a warning when using multiple implementation with the same name" in {
    final class X(xName: Symbol, info: ReflexiveInfo)
        extends Composite(xName, info) {
      val c: Initiator = Initiator()

      def this()(using givenInfo: ReflexiveInfo) = {
        this("X", givenInfo)
      }
    }

    final class Y(yName: Symbol, info: ReflexiveInfo)
        extends Composite(yName, info) {
      val x = X()

      def this()(using givenInfo: ReflexiveInfo) = {
        this("Y", givenInfo)
      }
    }

    val y1 = Y()
    val y2 = Y()

    y1.name should equal(y2.name)
    y1.x.name should equal(y2.x.name)
    y1.x.c.name should equal(y2.x.c.name)
    y1 should not equal (y2)
    y1.x should not equal (y2.x)
    y1.x.c should equal(y2.x.c)
  }

}
