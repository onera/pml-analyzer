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
import onera.pmlanalyzer.pml.model.service.{Load, Service, Store}
import onera.pmlanalyzer.pml.model.software.Application
import onera.pmlanalyzer.pml.model.utils.{Message, Owner, ReflexiveInfo}
import onera.pmlanalyzer.pml.model.{PMLNode, PMLNodeBuilder}
import onera.pmlanalyzer.pml.operators.*
import sourcecode.{File, Line, Name}
import onera.pmlanalyzer.views.interference.model.specification.InterferenceSpecification
import onera.pmlanalyzer.views.interference.model.specification.InterferenceSpecification.{
  PhysicalTransaction,
  PhysicalTransactionId
}

import scala.reflect.ClassTag

/** Base trait for library of transactions
  */
trait TransactionLibrary {
  self: Platform =>

  /** Map from the user defined transaction to the physical transaction id this
    * map does not contain user transactions with multi-path (contained in ...)
    * WARNING: this lazy variable can be called ONLY AFTER TRANSACTION/SCENARIO
    * DEFINITION
    * @group user_transaction_relation
    */
  final lazy val transactionByUserName
      : Map[UserTransactionId, PhysicalTransactionId] =
    UsedTransaction.all
      .flatMap(u =>
        for { t <- u.toPhysical(transactionsByName) } yield u.userName -> t
      )
      .toMap

  /** Map from the physical transaction id to the user defined id(s) It is
    * possible that a physical transaction is linked to several (or none) user
    * defined transactions WARNING: this lazy variable can be called ONLY AFTER
    * TRANSACTION/SCENARIO DEFINITION
    * @group user_transaction_relation
    */
  final lazy val transactionUserName
      : Map[PhysicalTransactionId, Set[UserTransactionId]] =
    transactionByUserName.keySet
      .groupMap(k => transactionByUserName(k))(k => k)
      .withDefaultValue(Set.empty)

  /** Map from the user defined scenario to the physical transaction id WARNING:
    * this lazy variable can be called ONLY AFTER TRANSACTION/SCENARIO
    * DEFINITION
    * @group user_scenario_relation
    */
  final lazy val scenarioByUserName
      : Map[UserScenarioId, Set[PhysicalTransactionId]] = {
    (transactionByUserName.keySet.map(k =>
      UserScenarioId(k.id) -> Set(transactionByUserName(k))
    ) ++
      UsedScenario.all.map(u => u.userName -> u.toPhysical(transactionsByName)))
      .groupMapReduce(_._1)(_._2)(_ ++ _)
  }

  /** Map from the physical scenario id (set of transaction id) to the user
    * defined scenario(s) It is possible that a scenario is linked to several
    * (or none) user defined scenarios WARNING: this lazy variable can be called
    * ONLY AFTER TRANSACTION/SCENARIO DEFINITION
    * @group user_scenario_relation
    */
  final lazy val scenarioUserName
      : Map[Set[PhysicalTransactionId], Set[UserScenarioId]] = {
    val result = scenarioByUserName.keySet
      .groupMap(k => scenarioByUserName(k))(k => k)
      .withDefaultValue(Set.empty)
    checkLibrary(transactionUserName, result)
    result
  }

  /** Map from the used scenario and the application involved in these scenarios
    * WARNING: this lazy variable can be called ONLY AFTER TRANSACTION/SCENARIO
    * DEFINITION
    * @group user_scenario_relation
    */
  final lazy val scenarioSW: Map[UserScenarioId, Set[Application]] = {
    transactionByUserName
    (UsedTransaction.all.map(k => UserScenarioId(k.name) -> k.sw) ++
      UsedScenario.all.map(k => k.userName -> k.sw))
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
      tMap: Map[PhysicalTransactionId, Set[UserTransactionId]],
      sMap: Map[Set[PhysicalTransactionId], Set[UserScenarioId]]
  ): Unit = {
    for (k <- transactionsByName.keySet) {
      if (
        (!tMap
          .contains(k) || tMap(k).isEmpty) && !sMap.keySet.flatten.contains(k)
      )
        println(Message.transactionNoInLibraryWarning(k))
      for { s <- tMap.get(k) if s.size >= 2 } yield println(
        Message.transactionHasSeveralNameWarning(k, s)
      )
    }
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

  /** Transaction extension method
    * @group transaction_operation
    * @param x
    *   id of the user transaction
    */
  given ToServicePath[UserTransactionId] with {
    def apply(x: UserTransactionId): Set[PhysicalTransaction] =
      (for {
        id <- transactionByUserName.get(x)
      } yield Set(transactionsByName(id))) getOrElse Set.empty
  }

  /** Scenario extension method
    * @group scenario_operation
    * @param x
    *   id of the user scenario
    */
  given ToServicePath[UserScenarioId] with {
    def apply(x: UserScenarioId): Set[PhysicalTransaction] =
      scenarioByUserName(x).flatMap(_.paths)
  }
}

object TransactionLibrary {

  /** Base trait for user ids
    */
  sealed abstract class UserId {
    val id: Symbol
    override def toString: String = id.name
  }

  /** User id for transactions
    * @param id
    *   name of the transaction
    */
  final case class UserTransactionId(id: Symbol) extends UserId

  /** User id of scenarios
    * @param id
    *   name of the scenario
    */
  final case class UserScenarioId(id: Symbol) extends UserId
}
