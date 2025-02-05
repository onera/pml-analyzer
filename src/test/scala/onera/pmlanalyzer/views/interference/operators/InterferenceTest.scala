package onera.pmlanalyzer.views.operators

import onera.pmlanalyzer.views.operators.*
import onera.pmlanalyzer.pml.operators.*
import onera.pmlanalyzer.pml.model.hardware.*
import onera.pmlanalyzer.views.interference.model.specification.PhysicalTableBasedInterferenceSpecification
import onera.pmlanalyzer.views.interference.operators.*
import onera.pmlanalyzer.pml.model.hardware.Platform
import onera.pmlanalyzer.views.interference.model.relations.{
  InterfereRelation,
  NotInterfereRelation
}

import sourcecode.Name
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.matchers.should
import onera.pmlanalyzer.pml.model.hardware.SimpleTransporter

import onera.pmlanalyzer.pml.examples.mySys.MyProcPlatform

class InterferenceTest extends AnyFlatSpecLike with should.Matchers {

  object PlatformFixture extends Platform(Symbol("fixture"))

  import PlatformFixture.*
  // "Two Hardwares" should "be able to interact with each other"
  "Two Hardwares" should "be able to interact with each other" in {
    val i1: Initiator = Initiator()
    val i2: Initiator = Initiator()
    val st1: SimpleTransporter = SimpleTransporter()
    val st2: SimpleTransporter = SimpleTransporter()
    val t1: Target = Target()
    val t2: Target = Target()

    for (
      (a, b) <- List(i1, i2, st1, st2, t1, t2).zip(
        List(i1, i2, st1, st2, t1, t2)
      )
    ) {
      a interfereWith b

    }
  }

  "Two Hardwares" should "be able not to interact with each other" in {
    val i1: Initiator = Initiator()
    val i2: Initiator = Initiator()
    val st1: SimpleTransporter = SimpleTransporter()
    val st2: SimpleTransporter = SimpleTransporter()
    val t1: Target = Target()
    val t2: Target = Target()

    for (
      (a, b) <- List(i1, i2, st1, st2, t1, t2).zip(
        List(i1, i2, st1, st2, t1, t2)
      )
    ) {
      a notInterfereWith b

    }
  }

  // "A Hardware" should "be able to have interfering services"
  // "A Hardware" should "be able to have non-interfering services"
}
// Tests seulement pour la relation Hadrware/Hardware ?
