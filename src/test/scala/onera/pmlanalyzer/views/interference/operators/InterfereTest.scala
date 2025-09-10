package onera.pmlanalyzer.views.interference.operators

import onera.pmlanalyzer.pml.model.configuration.*
import onera.pmlanalyzer.pml.model.hardware.*
import onera.pmlanalyzer.pml.model.software.{Application, Data}
import onera.pmlanalyzer.pml.operators.*
import onera.pmlanalyzer.pml.operators.Transform.TransactionLibraryInstances
import onera.pmlanalyzer.views.interference.InterferenceTestExtension.UnitTests
import onera.pmlanalyzer.views.interference.model.specification.InterferenceSpecification.AtomicTransactionId
import onera.pmlanalyzer.views.interference.model.specification.PhysicalTableBasedInterferenceSpecification
import onera.pmlanalyzer.views.interference.operators.*
import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.matchers.should
import sourcecode.Name

import scala.language.postfixOps

class InterfereTest extends AnyFlatSpecLike with should.Matchers {

  object InterfereTestPlatform
      extends Platform(Symbol("InterfereTestPlatform"))
      with PhysicalTableBasedInterferenceSpecification
      with TransactionLibraryInstances
      with TransactionLibrary {
    val i1: Initiator = Initiator()
    val i2: Initiator = Initiator()
    val st1: SimpleTransporter = SimpleTransporter()
    val st2: SimpleTransporter = SimpleTransporter()
    val t1: Target = Target()
    val t2: Target = Target()

    val app1: Application = Application()
    val app2: Application = Application()
    app1 hostedBy (i1)
    app2 hostedBy (i2)

    val d1: Data = Data()
    val d2: Data = Data()
    d1 hostedBy (t1)
    d2 hostedBy (t2)

    val tr1: Transaction = Transaction(app1 read d1)
    val tr2: Transaction = Transaction(app2 read d2)

    val tr1Id: AtomicTransactionId = AtomicTransactionId(
      Symbol("tr1")
    )
    val tr2Id: AtomicTransactionId = AtomicTransactionId(
      Symbol("tr2")
    )

    val scn: Transaction = Transaction(tr1, tr2)
  }

  import InterfereTestPlatform.{*, given}

  "Two services" should "be able to interfere with each other" taggedAs UnitTests in {
    for {
      a <- InterfereTestPlatform.hardware
      b <- InterfereTestPlatform.hardware
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

  it should "be able not to interfere with each other" taggedAs UnitTests in {
    for {
      a <- InterfereTestPlatform.hardware
      b <- InterfereTestPlatform.hardware
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

  "A transaction and a service" should "be able to interfere" taggedAs UnitTests in {
    for {
      s <- i1.services
    } {
      tr1Id interfereWith s
    }
    for {
      s <- i1.services
    } {
      physicalTransactionIdInterfereWithService(tr1Id) should contain(s)
    }
  }

  it should "be able not to interfere" taggedAs UnitTests in {
    for {
      s <- i2.services
    } {
      tr2Id notInterfereWith s
    }
    for {
      s <- i2.services
    } {
      physicalTransactionIdNotInterfereWithService(tr2Id) should contain(s)
    }
  }

  "A Scenario and a set of physical transaction" should "be able to interfere" taggedAs UnitTests in {
    for {
      s <- i1.services
    } {
      scn interfereWith s
    }
  }

  "Two Hardwares components" should "be able to interfere with each other" taggedAs UnitTests in {
    for {
      a <- InterfereTestPlatform.hardware
      b <- InterfereTestPlatform.hardware
    } {
      a interfereWith b
      hardwareInterfere(a) should contain(b)
    }
  }

  it should "be able not to interfere with each other" taggedAs UnitTests in {
    for {
      a <- InterfereTestPlatform.hardware
      b <- InterfereTestPlatform.hardware
    } {
      a notInterfereWith b
      // hardwareInterfere(a) should not contain (b)
      hardwareNotInterfere(a) should contain(b)
    }
    InterfereTestPlatform.hardware.size should be(6)
  }

  "A Hardware component" should "be able to have interfering services" taggedAs UnitTests in {
    for {
      a <- InterfereTestPlatform.hardware
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

  it should "be able to have non-interfering services" taggedAs UnitTests in {
    for {
      a <- InterfereTestPlatform.hardware
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
