package onera.pmlanalyzer.views.interference.operators

import onera.pmlanalyzer.pml.model.hardware.*
import onera.pmlanalyzer.views.interference.InterferenceTestExtension.UnitTests
import onera.pmlanalyzer.views.interference.model.specification.InterferenceSpecification.PhysicalTransactionId
import onera.pmlanalyzer.views.interference.model.specification.PhysicalTableBasedInterferenceSpecification
import onera.pmlanalyzer.views.interference.operators.*
import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.matchers.should

import scala.language.postfixOps

class TransparentTest extends AnyFlatSpecLike with should.Matchers {

  object TransparentTestPlatform
      extends Platform(Symbol("TransparentTestPlatform"))
      with PhysicalTableBasedInterferenceSpecification {
    val tr1Id: PhysicalTransactionId = PhysicalTransactionId(Symbol("tr1"))
    val tr2Id: PhysicalTransactionId = PhysicalTransactionId(Symbol("tr2"))
  }

  import TransparentTestPlatform.*

  "A transaction" should "be able to be transparent" taggedAs UnitTests in {
    tr1Id.isTransparent
    tr2Id.isTransparent
    transactionIsTransparent.value contains (tr1Id)
    transactionIsTransparent.value contains (tr2Id)
  }
}
