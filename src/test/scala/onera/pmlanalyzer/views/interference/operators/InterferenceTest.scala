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

import scala.language.postfixOps

class InterferenceTest extends AnyFlatSpecLike with should.Matchers {

  object PlatformFixture
      extends Platform(Symbol("fixture"))
      with PhysicalTableBasedInterferenceSpecification {
    val i1: Initiator = Initiator()
    val i2: Initiator = Initiator()
    val st1: SimpleTransporter = SimpleTransporter()
    val st2: SimpleTransporter = SimpleTransporter()
    val t1: Target = Target()
    val t2: Target = Target()
  }

  import PlatformFixture.*

  "Two services" should "be able to interfere with each other" in {
    for {
      a <- PlatformFixture.hardware
      b <- PlatformFixture.hardware
    } {
      for {
        l <- a.services
        r <- b.services
      } {
        l interfereWith r
        serviceInterfere(l) should contain(r)
      }
    }
  }

  "Two services" should "be able not to interfere with each other" in {
    for {
      a <- PlatformFixture.hardware
      b <- PlatformFixture.hardware
    } {
      for {
        l <- a.services
        r <- b.services
      } {
        l notInterfereWith r
        serviceNotInterfere(l) should contain(r)
      }
    }
  }

  "Two Hardwares" should "be able to interfere with each other" in {
    for {
      a <- PlatformFixture.hardware
      b <- PlatformFixture.hardware
    } {
      a interfereWith b
      hardwareInterfere(a) should contain(b)
    }
    // PlatformFixture.hardware.size should be(6)
    // hardwareInterfere(i1) should contain(i2)
  }

  "Two Hardwares" should "be able not to interfere with each other" in {
    for {
      a <- PlatformFixture.hardware
      b <- PlatformFixture.hardware
    } {
      a notInterfereWith b
      // hardwareInterfere(a) should not contain (b)
      hardwareNotInterfere(a) should contain(b)
    }
    PlatformFixture.hardware.size should be(6)
  }

  "A Hardware" should "be able to have interfering services" in {
    for {
      a <- PlatformFixture.hardware
    } {
      a.hasInterferingServices
      for {
        b1 <- a.services
        b2 <- a.services
      } {
        serviceInterfere(b1) should contain(b2)
      }
    }
  }

  "A Hardware" should "be able to have non-interfering services" in {
    for {
      a <- PlatformFixture.hardware
    } {
      a.hasNonInterferingServices
      for {
        b1 <- a.services
        b2 <- a.services
      } {
        serviceNotInterfere(b1) should contain(b2)
      }
    }
  }
}
