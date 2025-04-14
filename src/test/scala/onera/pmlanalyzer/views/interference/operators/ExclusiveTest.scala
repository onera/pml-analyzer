package onera.pmlanalyzer.views.interference.operators

import onera.pmlanalyzer.views.interference.model.relations.ExclusiveRelation
import onera.pmlanalyzer.views.operators.*
import onera.pmlanalyzer.pml.operators.*
import onera.pmlanalyzer.pml.model.hardware.*
import onera.pmlanalyzer.views.interference.model.specification.PhysicalTableBasedInterferenceSpecification
import onera.pmlanalyzer.pml.model.configuration.TransactionLibrary
import onera.pmlanalyzer.pml.model.software.{Application, Data}
import onera.pmlanalyzer.views.interference.operators.*
import onera.pmlanalyzer.pml.model.hardware.Platform

import sourcecode.Name
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.matchers.should
import onera.pmlanalyzer.views.interference.model.specification.InterferenceSpecification.{
  PhysicalTransaction,
  PhysicalTransactionId
}

import scala.language.postfixOps

class ExclusiveTest extends AnyFlatSpecLike with should.Matchers {

  object PlatformFixture
      extends Platform(Symbol("fixture"))
      with PhysicalTableBasedInterferenceSpecification {
    val tr1Id: PhysicalTransactionId = PhysicalTransactionId(Symbol("tr1"))
    val tr2Id: PhysicalTransactionId = PhysicalTransactionId(Symbol("tr2"))
  }

  import PlatformFixture.*

  "Two transaction" should "be able to be exclusive from each other" in {
    tr1Id exclusiveWith tr2Id
    transactionExclusive(tr1Id) should contain(tr2Id)
  }
}
