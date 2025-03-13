package onera.pmlanalyzer.views.interference.operators

import onera.pmlanalyzer.views.interference.model.relations.ExclusiveRelation
import onera.pmlanalyzer.views.operators.*
import onera.pmlanalyzer.pml.operators.*
import onera.pmlanalyzer.pml.model.hardware.*
import onera.pmlanalyzer.views.interference.operators.*
import onera.pmlanalyzer.views.interference.model.specification.PhysicalTableBasedInterferenceSpecification
import onera.pmlanalyzer.pml.model.configuration.TransactionLibrary
import onera.pmlanalyzer.pml.model.software.{Application, Data}
import onera.pmlanalyzer.views.interference.operators.*
import onera.pmlanalyzer.pml.model.hardware.Platform

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

  object PlatformFixture
      extends Platform(Symbol("fixture"))
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

    i1 link st1

    st1 link t1

    i2 link st2

    st2 link t2

    tr1 used

    tr2 used

    val us1 : UserTransactionId = UserTransactionId(Symbol("us1"))
    val us2 : UserTransactionId = UserTransactionId(Symbol("us2"))

    // val uTId: UserTransactionId = UserTransactionId(Symbol("uTId"))

    // val scn: Scenario = Scenario()
  }

  import PlatformFixture.*

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

  // "Two user transaction Id" should "be able to be exclusive" in {
  //   us1 exclusiveWith us2 
  //   transactionExclusive(transactionByUserName(us1)) should contain(transactionByUserName(us2))
  // }
}
