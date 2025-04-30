/** *****************************************************************************
 * Copyright (c) 2023. ONERA This file is part of PML Analyzer
 *
 * PML Analyzer is free software ; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation ; either version 2 of the License, or (at your
 * option) any later version.
 *
 * PML Analyzer is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY ; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program ; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package onera.pmlanalyzer.pml.operators

import onera.pmlanalyzer.pml.operators.*
import onera.pmlanalyzer.pml.model.hardware.*
import onera.pmlanalyzer.pml.model.service.Service
import onera.pmlanalyzer.pml.model.configuration.TransactionLibrary
import onera.pmlanalyzer.pml.model.software.{Application, Data}
import onera.pmlanalyzer.pml.model.relations.{
  CapacityRelation,
  DemandRelation,
  ProvideRelation
}
import sourcecode.{File, Line}
import onera.pmlanalyzer.views.interference.operators.*
import onera.pmlanalyzer.views.interference.model.specification.{
  ApplicativeTableBasedInterferenceSpecification,
  PhysicalTableBasedInterferenceSpecification
}
import onera.pmlanalyzer.views.interference.model.specification.InterferenceSpecification.{
  PhysicalTransaction,
  PhysicalTransactionId,
  PhysicalScenario,
  PhysicalScenarioId
}
import onera.pmlanalyzer.views.dependability.model.Transition
import onera.pmlanalyzer.views.interference.operators.Transform.{
  TransactionLibraryInstances,
  InterferenceSpecificationInstances
}
import onera.pmlanalyzer.views.interference.operators.*
import onera.pmlanalyzer.pml.model.relations.*
import sourcecode.Name
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.matchers.should
import scala.language.postfixOps

class DemandTest extends AnyFlatSpecLike with should.Matchers {
  object DemandTestPlatform
      extends Platform(Symbol("DemandTestPlatform"))
      with PhysicalTableBasedInterferenceSpecification
      with TransactionLibraryInstances
      with InterferenceSpecificationInstances
      with Relation.Instances
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

//    tr3 used
//
//    tr4 used

    val sc1: Scenario = Scenario(tr3, tr4)
    val sc2: Scenario = Scenario(tr1, tr2)

    sc1 used

    sc2 used

    val psIdSc1: PhysicalScenarioId = scenarioId(
      scenarioByUserName(sc1.userName)
    )
  }

  import DemandTestPlatform.{*, given}

  "A Transaction" should "have a demand" in {
    tr1.userName hasDemand 3
    demandOfTransaction(transactionByUserName(tr1.userName)) shouldBe 3
  }

  "A UserScenarioId" should "be associated to a demand" in {
    sc1.userName hasDemand 5
    for {
      pt <- scenarioByUserName(sc1.userName)
    } yield demandOfTransaction(pt) shouldBe 5
  }

  "A PhysicalScenarioId" should "be associated to a capacity" in {
    psIdSc1 hasDemand 4
    for {
      tr <- purifiedScenarios(psIdSc1)
    } yield demandOfTransaction(tr) shouldBe 4
  }
}
