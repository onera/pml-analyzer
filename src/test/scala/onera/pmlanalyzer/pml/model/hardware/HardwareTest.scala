package onera.pmlanalyzer.pml.model.hardware

import onera.pmlanalyzer.pml.model.hardware.*
import onera.pmlanalyzer.pml.model.service.{Load, Store}
import onera.pmlanalyzer.pml.model.utils.{Owner, ReflexiveInfo}
import onera.pmlanalyzer.pml.operators.*
import sourcecode.{File, Line, Name}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should
import onera.pmlanalyzer.views.interference.InterferenceTestExtension.UnitTests

class HardwareTest extends AnyFlatSpec with should.Matchers {

  /* Create a default platform to act as a container for components. */
  object HardwareTestFixture extends Platform(Symbol("HardwareTestFixture"))

  import HardwareTestFixture.*

  "A Hardware" should "have default services" taggedAs UnitTests in {
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

  it should "have no services when specified" taggedAs UnitTests in {
    val tNoS: Target = Target(withDefaultServices = false)
    val sNoS: SimpleTransporter = SimpleTransporter(withDefaultServices = false)
    val iNoS: Initiator = Initiator(withDefaultServices = false)
    val vNoS: Virtualizer = Virtualizer(withDefaultServices = false)

    for (h <- List(tNoS, sNoS, iNoS, vNoS)) {
      h.services should be(empty)
    }
  }

  it should "have only specified services when specified" taggedAs UnitTests in {
    val tSpS: Target =
      Target(Set(Load("a"), Load("b")), withDefaultServices = false)
    tSpS.services.size should be(2)
    exactly(2, tSpS.services) should be(a[Load])

    val sSpS = Target(Set(Store("a")), withDefaultServices = false)
    sSpS.services.size should be(1)
    exactly(1, sSpS.services) should be(a[Store])

    val iSpS = Target(Set.empty, withDefaultServices = false)
    iSpS.services should be(empty)

    val jSpS = Target(withDefaultServices = false)
    jSpS.services should be(empty)
  }
}
