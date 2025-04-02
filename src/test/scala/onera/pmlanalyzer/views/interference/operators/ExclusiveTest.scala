package onera.pmlanalyzer.views.interference.operators

import onera.pmlanalyzer.views.interference.model.relations.ExclusiveRelation
import onera.pmlanalyzer.views.interference.operators.*
import onera.pmlanalyzer.pml.operators.*
import onera.pmlanalyzer.pml.model.hardware.*
import onera.pmlanalyzer.views.interference.operators.*
import onera.pmlanalyzer.views.interference.model.specification.{
  ApplicativeTableBasedInterferenceSpecification,
  PhysicalTableBasedInterferenceSpecification
}
import onera.pmlanalyzer.pml.model.configuration.TransactionLibrary
import onera.pmlanalyzer.pml.model.configuration.TransactionLibrary.UserTransactionId
import onera.pmlanalyzer.pml.model.software.{Application, Data}
import onera.pmlanalyzer.views.interference.operators.*
import onera.pmlanalyzer.pml.model.hardware.Platform
import onera.pmlanalyzer.views.interference.model.specification.InterferenceSpecification.PhysicalTransactionId
import sourcecode.Name
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.matchers.should
// import onera.pmlanalyzer.pml.model.hardware.SimpleTransporter
// import onera.pmlanalyzer.pml.examples.mySys.MyProcPlatform
import onera.pmlanalyzer.views.interference.model.specification.InterferenceSpecification.{
  PhysicalTransaction,
  PhysicalTransactionId
}
import onera.pmlanalyzer.views.dependability.model.Transition
import onera.pmlanalyzer.pml.model.configuration.TransactionLibrary
import onera.pmlanalyzer.views.interference.operators.Transform.TransactionLibraryInstances

import scala.language.postfixOps
import onera.pmlanalyzer.pml.model.configuration.TransactionLibrary.UserTransactionId

class ExclusiveTest extends AnyFlatSpecLike with should.Matchers {

  object ExclusiveTestPlatform
      extends Platform(Symbol("ExclusiveTestPlatform"))
      with PhysicalTableBasedInterferenceSpecification
      with TransactionLibraryInstances
      with TransactionLibrary {
    val tr1Id: PhysicalTransactionId = PhysicalTransactionId(Symbol("tr1"))
    val tr2Id: PhysicalTransactionId = PhysicalTransactionId(Symbol("tr2"))

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

  "Two physical transaction" should "be able to be exclusive from each other" in {
    tr1Id exclusiveWith tr2Id
    transactionExclusive(tr1Id) should contain(tr2Id)
  }

  "Two transaction" should "be able to be exclusive from each other" in {
    tr1 exclusiveWith tr2
    transactionExclusive(transactionByUserName(tr1.userName)) should contain(
      transactionByUserName(tr2.userName)
    )
  }

  "Two user transaction Id" should "be able to be exclusive" in {
    tr3.userName exclusiveWith tr4.userName
    transactionExclusive(
      transactionByUserName(tr3.userName)
    ) should contain(
      transactionByUserName(tr4.userName)
    )
  }
}
