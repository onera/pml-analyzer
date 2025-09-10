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

package onera.pmlanalyzer.pml.model.configuration

import onera.pmlanalyzer.pml.model.configuration.TransactionLibrary.*
import onera.pmlanalyzer.pml.model.hardware.Platform
import onera.pmlanalyzer.pml.model.software.Application
import onera.pmlanalyzer.pml.model.utils.Message
import onera.pmlanalyzer.pml.operators.*
import onera.pmlanalyzer.views.interference.model.specification.InterferenceSpecification
import onera.pmlanalyzer.views.interference.model.specification.InterferenceSpecification.{
  AtomicTransaction,
  AtomicTransactionId
}

/** Base trait for library of transactions
  */
trait TransactionLibrary extends Transform.TransactionLibraryInstances {
  self: Platform =>

  /** Map from the user defined scenario to the physical transaction id WARNING:
    * this lazy variable can be called ONLY AFTER TRANSACTION/SCENARIO
    * DEFINITION
    * @group user_scenario_relation
    */
  final lazy val scenarioByUserName
      : Map[UserTransactionId, Set[AtomicTransactionId]] = {
    UsedTransaction.all
      .map(u => u.userName -> u.toPhysical(atomicTransactionsByName))
      .groupMapReduce(_._1)(_._2)(_ ++ _)
  }

  /** Map from the physical scenario id (set of transaction id) to the user
    * defined scenario(s) It is possible that a scenario is linked to several
    * (or none) user defined scenarios WARNING: this lazy variable can be called
    * ONLY AFTER TRANSACTION/SCENARIO DEFINITION
    * @group user_scenario_relation
    */
  final lazy val scenarioUserName
      : Map[Set[AtomicTransactionId], Set[UserTransactionId]] = {
    val result = scenarioByUserName.keySet
      .groupMap(k => scenarioByUserName(k))(k => k)
      .withDefaultValue(Set.empty)
    checkLibrary(result)
    result
  }

  /** Map from the used scenario and the application involved in these scenarios
    * WARNING: this lazy variable can be called ONLY AFTER TRANSACTION/SCENARIO
    * DEFINITION
    * @group user_scenario_relation
    */
  final lazy val scenarioSW: Map[UserTransactionId, Set[Application]] = {
    UsedTransaction.all
      .map(k => k.userName -> k.sw)
      .groupMapReduce(_._1)(_._2)(_ ++ _)
  }

  /** Check the transaction and scenario libraries w.r.t. the transactions
    * computed with the actual the ideal (but not requested situation) is
    * one-to-one libraries definition of the platform
    * @group utilFun
    * @param tMap
    *   the transaction library to check
    * @param sMap
    *   the scenario library to check
    */
  final def checkLibrary(
      sMap: Map[Set[AtomicTransactionId], Set[UserTransactionId]]
  ): Unit = {
    this match {
      case i: InterferenceSpecification =>
        for (
          (s, st) <- i.purifiedScenarios
          if !sMap.contains(st) || sMap(st).isEmpty
        ) {
          println(Message.scenarioNotInLibraryWarning(s))
        }
    }
  }
}

object TransactionLibrary {

  /** User id of scenarios
    * @param id
    *   name of the scenario
    */
  final case class UserTransactionId(id: Symbol) {
    override def toString: String = id.name
  }
}
