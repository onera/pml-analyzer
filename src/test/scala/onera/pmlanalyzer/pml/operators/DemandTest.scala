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

import onera.pmlanalyzer.*
import onera.pmlanalyzer.pml.model.relations.{DemandRelation, ProvideRelation}
import onera.pmlanalyzer.views.interference.InterferenceTestExtension.UnitTests
import onera.pmlanalyzer.views.interference.model.specification.InterferenceSpecification.AtomicTransactionId
import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.matchers.should
import sourcecode.{File, Line, Name}

import scala.language.postfixOps

class DemandTest extends AnyFlatSpecLike with should.Matchers {
  object DemandTestPlatform
      extends Platform(Symbol("DemandTestPlatform"))
      with PhysicalTableBasedInterferenceSpecification
      with TransactionLibrary {
    val tr1Id: AtomicTransactionId = AtomicTransactionId(Symbol("tr1"))
    val tr2Id: AtomicTransactionId = AtomicTransactionId(Symbol("tr2"))

    val i1: Initiator = Initiator()
    val i2: Initiator = Initiator()
    val st1: SimpleTransporter = SimpleTransporter()
    val st2: SimpleTransporter = SimpleTransporter()
    val t1: Target = Target()
    val t2: Target = Target()

    val app1: Application = Application()
    val app2: Application = Application()
    app1 hostedBy i1
    app2 hostedBy i2

    val d1: Data = Data()
    val d2: Data = Data()
    d1 hostedBy t1
    d2 hostedBy t2

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
  }

  import DemandTestPlatform.{*, given}

  "An AtomictransactionId" should "has a demand" taggedAs UnitTests in {
    tr1Id hasDemand 2
    context.demandOfTransaction(tr1Id) shouldBe 2
  }

  "A UserTransactionId" should "has a demand" taggedAs UnitTests in {
    tr1.userName hasDemand 3
    for { at <- transactionByUserName(tr1.userName) } {
      context.demandOfTransaction(at) shouldBe 3
    }
  }

  "A Transaction" should "has a demand" taggedAs UnitTests in {
    tr2 hasDemand 4
    for { at <- transactionByUserName(tr2.userName) } {
      context.demandOfTransaction(at) shouldBe 4
    }
  }

  "An Application" should "be associated to a demand" taggedAs UnitTests in {
    app1 hasDemand 5
    for { at <- atomicTransactionsBySW(app1) } {
      context.demandOfTransaction(at) shouldBe 5
    }
  }
}
