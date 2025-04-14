package onera.pmlanalyzer.views.interference.operators

import onera.pmlanalyzer.pml.model.configuration.*
import onera.pmlanalyzer.pml.model.hardware.*
import onera.pmlanalyzer.views.interference.model.specification.{
  ApplicativeTableBasedInterferenceSpecification,
  PhysicalTableBasedInterferenceSpecification
}
import onera.pmlanalyzer.pml.model.configuration.TransactionLibrary
import onera.pmlanalyzer.pml.model.configuration.TransactionLibrary.UserTransactionId
import onera.pmlanalyzer.pml.model.software.{Application, Data}
import onera.pmlanalyzer.pml.operators.*
import onera.pmlanalyzer.pml.operators.Transform.TransactionLibraryInstances
import onera.pmlanalyzer.views.interference.InterferenceTestExtension.UnitTests
import onera.pmlanalyzer.views.interference.model.specification.ApplicativeTableBasedInterferenceSpecification
import onera.pmlanalyzer.views.interference.model.specification.InterferenceSpecification.AtomicTransactionId
import onera.pmlanalyzer.views.interference.operators.*
import onera.pmlanalyzer.pml.model.hardware.Platform
import onera.pmlanalyzer.views.interference.model.specification.InterferenceSpecification.PhysicalTransactionId
import sourcecode.Name
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.matchers.should
import sourcecode.Name

import scala.language.postfixOps
import onera.pmlanalyzer.pml.model.configuration.TransactionLibrary.UserTransactionId

class ExclusiveTest extends AnyFlatSpecLike with should.Matchers {

  object ExclusiveTestPlatform
      extends Platform(Symbol("ExclusiveTestPlatform"))
      with ApplicativeTableBasedInterferenceSpecification
      with TransactionLibraryInstances
      with TransactionLibrary {
    val tr1Id: AtomicTransactionId = AtomicTransactionId(
      Symbol("tr1")
    )
    val tr2Id: AtomicTransactionId = AtomicTransactionId(
      Symbol("tr2")
    )

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

    val tr3: Transaction = Transaction(app1 read d1)
    val tr4: Transaction = Transaction(app2 read d2)

    i1 link st1

    st1 link t1

    i2 link st2

    st2 link t2

    tr1 used

    tr2 used

    tr3 used

    tr4 used
  }

  import ExclusiveTestPlatform.{*, given}

  "Two physical transaction" should "be able to be exclusive from each other" taggedAs UnitTests in {
    tr1Id exclusiveWith tr2Id
    transactionExclusive(tr1Id) should contain(tr2Id)
  }

  "Two transaction" should "be able to be exclusive from each other" taggedAs UnitTests in {
    tr1 exclusiveWith tr2
    userTransactionExclusive(tr1.userName) should contain(tr2.userName)
  }

  "Two user transaction Id" should "be able to be exclusive" taggedAs UnitTests in {
    tr3.userName exclusiveWith tr4.userName
    userTransactionExclusive(tr3.userName) should contain(tr4.userName)
  }
}
