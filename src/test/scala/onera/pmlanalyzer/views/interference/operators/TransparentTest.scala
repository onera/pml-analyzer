package onera.pmlanalyzer.views.interference.operators

import onera.pmlanalyzer.pml.model.configuration.TransactionLibrary
import onera.pmlanalyzer.pml.model.hardware.*
import onera.pmlanalyzer.pml.model.software.{Application, Data}
import onera.pmlanalyzer.pml.operators.*
import onera.pmlanalyzer.views.interference.model.relations.ExclusiveRelation
import onera.pmlanalyzer.views.interference.model.specification.PhysicalTableBasedInterferenceSpecification
import onera.pmlanalyzer.views.interference.operators.*
import org.scalatest.flatspec.{AnyFlatSpec, AnyFlatSpecLike}
import org.scalatest.matchers.should
import sourcecode.Name
import onera.pmlanalyzer.views.interference.model.specification.InterferenceSpecification.{
  PhysicalTransaction,
  PhysicalTransactionId
}

import scala.language.postfixOps

class TransparentTest extends AnyFlatSpecLike with should.Matchers {

  object TransparentTestPlatform
      extends Platform(Symbol("TransparentTestPlatform"))
      with PhysicalTableBasedInterferenceSpecification {
    val tr1Id: PhysicalTransactionId = PhysicalTransactionId(Symbol("tr1"))
    val tr2Id: PhysicalTransactionId = PhysicalTransactionId(Symbol("tr2"))
  }

  import TransparentTestPlatform.*

  "A transaction" should "be able to be transparent" in {
    tr1Id.isTransparent
    tr2Id.isTransparent
    transactionIsTransparent.value contains (tr1Id)
    transactionIsTransparent.value contains (tr2Id)
  }
}
