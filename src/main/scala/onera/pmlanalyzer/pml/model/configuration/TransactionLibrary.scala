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
import onera.pmlanalyzer.pml.model.utils.{Message, Owner}
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
      .flatMap(u => for { t <- u.toPhysical } yield u.userName -> t)
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
      UsedScenario.all.map(u => u.userName -> u.toPhysical))
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

  /** Encode node that are either transactions or scenarios
    * @group scenario_class
    * @param name
    *   the name of the transaction or scenario
    */
  sealed abstract class ScenarioLike(val name: Symbol, line: Line, file: File)
    extends PMLNode(line, file) {
    val iniTgt: () => Set[(Service, Service)]
    val sw: () => Set[Application]
  }

  /** Class encoding the used defined transactions (not already used)
    * @group transaction_class
    * @param userName
    *   the name of the node
    * @param iniTgt
    *   a by-name value providing the origin-destination service couples of the
    *   transaction (not evaluated during the object initialisation)
    * @param sw
    *   the application that can use this transaction
    */
  final class Transaction private (
      val userName: UserTransactionId,
      val iniTgt: () => Set[(Service, Service)],
      val sw: () => Set[Application],
      line: Line,
      file: File
                                  ) extends ScenarioLike(userName.id, line, file) {

    /** Consider the transaction for the analysis
      *
      * @return
      *   the used transaction
      */
    def used(using givenLine: Line, givenFile: File): UsedTransaction =
      UsedTransaction(userName, iniTgt(), sw())

    override def toString: String = s"$userName"
  }

  /** Builder of platform [[Transaction]]
    * @group transaction_class
    */
  object Transaction extends PMLNodeBuilder[Transaction] {

    /** A transaction can be built from an application targeting a load or a
      * store service
      *
      * @param iniTgt
      *   the application/target service used
      * @param name
      *   the implicit name of the transaction (deduced from val used during
      *   instantiation)
      * @tparam A
      *   the type of requests
      * @return
      *   the transaction (not used for now)
      */
    def apply[A: AsTransaction](
        iniTgt: => A
                               )(using name: Name, line: Line, file: File): Transaction = {
      val result = TransactionParam(iniTgt)
      apply(UserTransactionId(Symbol(name.value)), result._1, result._2)
    }

    /** Main constructor of a transaction, note that transaction are memoized,
      * so if the same name is used in the same platform the constructor will
      * send back the previous definition of the transaction
      *
      * @param name
      *   the name of the transaction
      * @param iniTgt
      *   the set of initial/target services defining the transaction
      * @param sw
      *   the applications that may invoke this transaction
      * @param owner
      *   the owner of the transaction (the platform)
      * @return
      *   the transaction (not used for now)
      */
    def apply(
        name: UserTransactionId,
        iniTgt: () => Set[(Service, Service)],
        sw: () => Set[Application]
             )(using owner: Owner, line: Line, file: File): Transaction = {
      _memo.getOrElseUpdate(
        (owner.s, name.id),
        new Transaction(name, iniTgt, sw, line, file)
      )
    }

    /** A transaction can be from an application targeting a load or a store
      * service
      *
      * @param name
      *   explicit name of the transaction
      * @param iniTgt
      *   the application/target service used
      * @tparam A
      *   the type of requests
      * @return
      *   the transaction (not used for now)
      */
    def apply[A: AsTransaction](name: String, iniTgt: => A)(using
                                                            line: Line,
                                                            file: File
    ): Transaction = {
      val result = TransactionParam(iniTgt)
      apply(UserTransactionId(name), result._1, result._2)
    }

    /** A transaction can be build from another transaction
      *
      * @param from
      *   the initial transaction
      * @param name
      *   the implicit name of the transaction (deduced from val used during
      *   instantiation)
      * @return
      *   the transaction (not used for now)
      */
    def apply(
               from: Transaction
             )(using name: Name, line: Line, file: File): Transaction =
      apply(UserTransactionId(Symbol(name.value)), from.iniTgt, from.sw)
  }

  /** Class encoding the defined transactions (not already used)
    * @group scenario_class
    * @param userName
    *   the name of the node
    * @param iniTgt
    *   a by-name value providing the origin-destination service couples of the
    *   scenario (not evaluated during the object initialisation)
    * @param sw
    *   the application that can use this scenario
    */
  final class Scenario private (
      val userName: UserScenarioId,
      val iniTgt: () => Set[(Service, Service)],
      val sw: () => Set[Application],
      line: Line,
      file: File
                               ) extends ScenarioLike(userName.id, line, file) {

    /** Consider the transaction for the analysis
      *
      * @return
      *   the used scenario class
      */
    def used(using givenLine: Line, givenFile: File): UsedScenario =
      UsedScenario(userName, iniTgt(), sw())
  }

  /** Builder of platform [[Scenario]]
    * @group scenario_class
    */
  object Scenario extends PMLNodeBuilder[Scenario] {

    /** Build scenario from two write/read based transactions
      * @param iniTgtL
      *   the set of initiator/target of left member
      * @param iniTgtR
      *   the set of initiator/target of right member
      * @param name
      *   the implicitly derived name
      * @tparam A
      *   the type of left request
      * @tparam B
      *   the type of right request
      * @return
      *   the corresponding scenario
      */
    def apply[A, B](iniTgtL: => Set[A], iniTgtR: => Set[B])(using
        name: Name,
        ta: AsTransaction[Set[A]],
                                                            tb: AsTransaction[Set[B]],
                                                            line: Line,
                                                            file: File
    ): Scenario = {
      val resultL = TransactionParam(iniTgtL)
      val resultR = TransactionParam(iniTgtR)
      apply(
        UserScenarioId(Symbol(name.value)),
        () => {
          resultL._1() ++ resultR._1()
        },
        () => {
          resultL._2() ++ resultR._2()
        }
      )
    }

    /** Build a scenario from a transaction or another scenario
      * @param tr
      *   the original scenario like
      * @param name
      *   the implicitly derived name
      * @return
      *   the resulting scenario
      */
    def apply(
               tr: ScenarioLike
             )(using name: Name, line: Line, file: File): Scenario =
      apply(UserScenarioId(Symbol(name.value)), tr.iniTgt, tr.sw)

    /** Build a Scenario from a bunch of transactions, this should not be used
      * with anonymous transaction
      *
      * @param tr
      *   the set of transactions
      * @param name
      *   the implicit name of the scenario (same as the variable used to refer
      *   to it)
      * @return
      *   a scenario
      */
    def apply(
               tr: Transaction*
             )(using name: Name, line: Line, file: File): Scenario =
      apply(
        UserScenarioId(Symbol(name.value)),
        () => {
          tr.flatMap(_.iniTgt()).toSet
        },
        () => {
          tr.flatMap(_.sw()).toSet
        }
      )

    /** Main constructor of a scenario, note that scenarios are memoized, so if
      * the same name is used in the same platform the constructor will send
      * back the previous definition of the scenario
      *
      * @param name
      *   the name of the scenario
      * @param iniTgt
      *   the set of initial/target services defining the scenario
      * @param sw
      *   the applications that may invoke this scenario
      * @param owner
      *   the owner of the transaction (the platform)
      * @return
      *   the transaction (not used for now)
      */
    def apply(
        name: UserScenarioId,
        iniTgt: () => Set[(Service, Service)],
        sw: () => Set[Application]
             )(using owner: Owner, line: Line, file: File): Scenario = {
      _memo.getOrElseUpdate(
        (owner.s, name.id),
        new Scenario(name, iniTgt, sw, line, file)
      )
    }
  }

  /** Class encoding the user defined scenarios used in the configuration
    * @group scenario_class
    * @param userName
    *   the name of the node
    * @param iniTgt
    *   the origin-destination services couples
    * @param sw
    *   the application that can use this scenario
    */
  final class UsedScenario private (
      val userName: UserScenarioId,
      iniTgt: Set[(Service, Service)],
      val sw: Set[Application],
      line: Line,
      file: File
                                   ) extends PMLNode(line, file) {

    val name: Symbol = userName.id

    /** Try to find a physical transaction of the scenario
      *
      * @return
      *   the set of physical transaction if possible
      */
    def toPhysical: Set[PhysicalTransactionId] = {
      iniTgt.flatMap(it =>
        transactionsByName
          .filter(p =>
            p._2.size >= 2 && it._1 == p._2.head && it._2 == p._2.last
          )
          .toList match {
          case Nil =>
            println(Message.impossibleScenarioWarning(userName))
            None
          case (k, _) :: Nil => Some(k)
          case h :: t =>
            println(Message.multiPathScenarioWarning(userName, h +: t))
            (h +: t).map(_._1)
        }
      )
    }
  }

  /** Builder of platform [[UsedScenario]]
    * @group scenario_class
    */
  object UsedScenario extends PMLNodeBuilder[UsedScenario] {

    /** Build a used scenario from its attributes
      * @param name
      *   the explicit name
      * @param iniTgt
      *   the initiator/target couples
      * @param sw
      *   the application that can use it
      * @param owner
      *   the implicitly derived owner of the scenario
      * @return
      *   the corresponding scenario (used in the interference analysis)
      */
    def apply(
        name: UserScenarioId,
        iniTgt: Set[(Service, Service)],
        sw: Set[Application]
             )(using owner: Owner, line: Line, file: File): UsedScenario = {
      _memo.getOrElseUpdate(
        (owner.s, name.id),
        new UsedScenario(name, iniTgt, sw, line, file)
      )
    }
  }

  /** Class encoding the user defined transactions used in the configuration
    * @group transaction_class
    * @param userName
    *   the name of the node
    * @param iniTgt
    *   the origin-destination services couples
    * @param sw
    *   the application that can use this transaction
    */
  final class UsedTransaction private (
      val userName: UserTransactionId,
      iniTgt: Iterable[(Service, Service)],
      val sw: Set[Application],
      line: Line,
      file: File
                                      ) extends PMLNode(line, file) {

    val name: Symbol = userName.id

    /** Try to find a physical transaction to the user transaction
      *
      * @return
      *   the physical transaction if possible
      */
    def toPhysical: Option[PhysicalTransactionId] = transactionsByName
      .filter(p =>
        p._2.size >= 2 && iniTgt
          .exists(it => it._1 == p._2.head && it._2 == p._2.last)
      )
      .toList match {
      case Nil =>
        println(Message.impossibleTransactionWarning(userName))
        None
      case (k, _) :: Nil => Some(k)
      case h :: t =>
        println(Message.multiPathTransactionWarning(userName, h +: t))
        Scenario(UserScenarioId(userName.id), () => iniTgt.toSet, () => sw).used
        None
    }
  }

  /** Builder of platform [[UsedTransaction]]
    * @group transaction_class
    */
  object UsedTransaction extends PMLNodeBuilder[UsedTransaction] {

    /** Main constructor of a used transaction, note that used transaction are
      * memoized, so if the same name is used in the same platform the
      * constructor will send back the previous definition of the used
      * transaction
      *
      * @param name
      *   the name of the used transaction
      * @param iniTgt
      *   the set of inital/target services defining the transaction
      * @param sw
      *   the application that may invoke this transaction
      * @param owner
      *   the platform owning the transaction
      * @return
      *   the used transaction
      */
    def apply(
        name: UserTransactionId,
        iniTgt: Iterable[(Service, Service)],
        sw: Set[Application]
             )(using owner: Owner, line: Line, file: File): UsedTransaction = {
      _memo.getOrElseUpdate(
        (owner.s, name.id),
        new UsedTransaction(name, iniTgt, sw, line, file)
      )
    }

  }

  /** Transaction extension method
    * @group transaction_operation
    * @param x
    *   id of the user transaction
    */
  final implicit class UserTransactionOps(x: UserTransactionId)
      extends TransactionLikeOps {
    def paths: Set[PhysicalTransaction] =
      (for {
        id <- transactionByUserName.get(x)
      } yield Set(transactionsByName(id))) getOrElse Set.empty
  }

  /** Scenario extension method
    * @group scenario_operation
    * @param x
    *   id of the user scenario
    */
  final implicit class ScenarioOps(x: UserScenarioId)
      extends TransactionLikeOps {
    def paths: Set[PhysicalTransaction] =
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
