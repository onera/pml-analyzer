/*******************************************************************************
 * Copyright (c)  2023. ONERA
 * This file is part of PML Analyzer
 *
 * PML Analyzer is free software ;
 * you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation ;
 * either version 2 of  the License, or (at your option) any later version.
 *
 * PML Analyzer is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY ;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this program ;
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA
 ******************************************************************************/

package onera.pmlanalyzer.views.interference.model.specification

import onera.pmlanalyzer.pml.model.configuration.TransactionLibrary
import onera.pmlanalyzer.pml.model.hardware.{Hardware, Platform}
import onera.pmlanalyzer.pml.model.service.Service
import onera.pmlanalyzer.pml.operators._
import onera.pmlanalyzer.views.interference.model.specification.InterferenceSpecification.{
  PhysicalScenario,
  PhysicalScenarioId,
  AtomicTransaction,
  AtomicTransactionId
}
import onera.pmlanalyzer.pml.operators.Transform

/** Base trait for all interference specification
  */
trait InterferenceSpecification {
  self: Platform =>

  /** Map from the physical transaction id and their service sequence
    * representation computed through an analysis of the platform WARNING: this
    * lazy variable MUST NOT be called during platform object initialisation
    * @group transaction_relation
    */
  final lazy val purifiedTransactions
      : Map[AtomicTransactionId, AtomicTransaction] =
    this.atomicTransactionsByName.transform((k, _) => purify(k))

  /** Map from the service sequence representation to their id WARNING: this
    * lazy variable MUST NOT be called during platform object initialisation
    * @group transaction_relation
    */
  final lazy val purifiedTransactionsName
      : Map[AtomicTransaction, AtomicTransactionId] =
    purifiedTransactions.groupMapReduce(_._2)(_._1)((l, _) => l)

  /** compute the considered scenarios depending on the configuration and the
    * (optional) library, a scenario is either: a named and used transaction
    * (e.g. val t = Transaction(a read b); t.used) a named and used scenario
    * (e.g. val s = Scenario(t1, t2); s.used) an anonymous copy (e.g. a copy r
    * on s) an anonymous transaction (e.g. a read b) not already involved in a
    * copy, a named scenario or a named transaction WARNING this will discard an
    * anonymous transaction defined inside and outside a copy, this issue does
    * not occur if we keep the segregation Smart/NonSmart
    * @group scenario_relation
    * @return
    *   the set of scenarios
    */
  final lazy val purifiedScenarios: Map[PhysicalScenarioId, PhysicalScenario] =
    this match {
      case l: TransactionLibrary =>
        val namedScenarios = l.scenarioByUserName
          .map(kv => scenarioId(kv._2) -> kv._2)
        val anonymousTransaction = purifiedTransactions
          .filter(kv => !namedScenarios.values.exists(_.contains(kv._1)))
          .map(kv => PhysicalScenarioId(kv._1.id) -> Set(kv._1))
        namedScenarios ++ anonymousTransaction
      case _ =>
        val anonymousTransaction = purifiedTransactions
          .map(kv => PhysicalScenarioId(kv._1.id) -> Set(kv._1))
        anonymousTransaction
    }

  /** Check whether a transaction is discarded during the analysis
    * @group transparent_predicate
    * @param t
    *   the identifier of the transaction
    * @return
    *   true is the transaction is discarded
    */
  def isTransparentTransaction(t: AtomicTransactionId): Boolean

  /** Provide the services a given transaction interfere with (additionally to
    * the one identified in its path)
    * @group interfere_predicate
    * @param t
    *   the identifier of the transaction
    * @return
    *   a set of services
    */
  def transactionInterfereWith(t: AtomicTransactionId): Set[Service]

  /** Provide the services a given transaction do not interfere with
    * @group interfere_predicate
    * @param t
    *   the identifier of the transaction
    * @return
    *   a set of services
    */
  def transactionNotInterfereWith(t: AtomicTransactionId): Set[Service]

  /** Check whether two hardware cannot work simultaneously
    * @group interfere_predicate
    * @param l
    *   the left hardware
    * @param r
    *   the right hardware
    * @return
    *   true if they cannot work simultaneously
    */
  final def finalInterfereWith(l: Hardware, r: Hardware): Boolean =
    antiReflexive(l, r) && symmetric[Hardware](interfereWith)(l, r)

  /** Check whether two transaction will not occur simultaneously
    * @group exclusive_predicate
    * @param l
    *   the left transaction
    * @param r
    *   the right transaction
    * @return
    *   true if they cannot occur simultaneously
    */
  final def finalExclusive(
      l: AtomicTransactionId,
      r: AtomicTransactionId
  ): Boolean = {
    antiReflexive(l, r) && symmetric[AtomicTransactionId](
      exclusiveWith
    )(l, r)
  }

  /** Check whether two scenarios will not occur simultaneously
    * @group exclusive_predicate
    * @param l
    *   the left scenarios
    * @param r
    *   the right scenarios
    * @return
    *   true if they cannot occur simultaneously
    */
  final def finalExclusive(
      l: PhysicalScenarioId,
      r: PhysicalScenarioId
  ): Boolean =
    antiReflexive(l, r) &&
      symmetric((le: PhysicalScenarioId, re: PhysicalScenarioId) =>
        purifiedScenarios(le).exists(t =>
          purifiedScenarios(re).exists(tp => finalExclusive(t, tp))
        )
      )(l, r)

  /** Provide the map encoding of finalInterfereWith
    * @group exclusive_predicate
    * @param s
    *   the set of scenario
    * @return
    *   the map encoding
    */
  final def finalExclusive(
      s: Set[PhysicalScenarioId]
  ): Map[PhysicalScenarioId, Set[PhysicalScenarioId]] =
    relationToMap(s, (l, r) => finalExclusive(l, r))

  // TODO Very dirty, should consider that an affect is a scenario
  /** Add the services of transactionInterfereWith to the path and remove the
    * ones of transactionNotInterfereWith
    * @group utilFun
    * @param t
    *   the identifier of the transaction
    * @return
    *   the path of the transaction
    */
  private final def purify(t: AtomicTransactionId): AtomicTransaction =
    atomicTransactionsByName.get(t) match {
      case Some(h :: tail) =>
        (h +: (transactionInterfereWith(t).toList.sortBy(_.name.name) ++ tail))
          .filterNot(transactionNotInterfereWith(t))
      case _ => Nil
    }

  /** Provide the equivalence classes over s with
    * [[views.interference.operators.Equivalent.Ops]] relation
    * @group equivalence_predicate
    * @param s
    *   the set of [[pml.model.service.Service]]
    * @return
    *   the equivalence classes
    */
  final def serviceEquivalenceClasses(s: Set[Service]): Set[Set[Service]] =
    equivalenceClasses[Service]((le, re) =>
      reflexive(le, re) || symmetric(areEquivalent)(le, re)
    )(s)

  /** Provide the equivalence classes over s with
    * [[views.interference.operators.Equivalent.Ops]] relation
    * @group equivalence_predicate
    * @param s
    *   the set of
    *   [[views.interference.model.specification.InterferenceSpecification.PhysicalTransactionId]]
    * @return
    *   the equivalence classes
    */
  final def equivalenceTransactionClasses(
      s: Set[AtomicTransactionId]
  ): Set[Set[AtomicTransactionId]] = {
    val serviceClasses = serviceEquivalenceClasses(
      s.flatMap(atomicTransactionsByName.get).flatMap(_.toSet)
    )
    val areEquivalentTr =
      (l: AtomicTransactionId, r: AtomicTransactionId) => {
        atomicTransactionsByName.contains(l) &&
        atomicTransactionsByName.contains(r) &&
        atomicTransactionsByName(l).size == atomicTransactionsByName(r).size &&
        atomicTransactionsByName(l)
          .zip(atomicTransactionsByName(r))
          .forall(p =>
            serviceClasses.exists(c => c.contains(p._1) && c.contains(p._2))
          )
      }

    equivalenceClasses[AtomicTransactionId]((le, re) =>
      reflexive(le, re) || symmetric(areEquivalentTr)(le, re)
    )(s)
  }

  /** Check if it exists at least one common service used by two set of
    * scenarios
    * @group utilFun
    * @param l
    *   the left set of scenarios
    * @param r
    *   the right set of scenarios
    * @return
    *   true whether one channel exists
    */
  final def channelNonEmpty(
      l: Set[PhysicalScenarioId],
      r: Set[PhysicalScenarioId]
  ): Boolean =
    l.flatMap(purifiedScenarios)
      .flatMap(purifiedTransactions)
      .exists(ls =>
        r.flatMap(purifiedScenarios)
          .flatMap(purifiedTransactions)
          .exists(rs => ls == rs || finalInterfereWith(ls, rs))
      )

  /** Provide the map encoding of channelNonEmpty
    * @group utilFun
    * @param s
    *   the set of gathered scenarios
    * @return
    *   the map encoding
    */
  final def channelNonEmpty(
      s: Set[Set[PhysicalScenarioId]]
  ): Map[Set[PhysicalScenarioId], Set[Set[PhysicalScenarioId]]] =
    relationToMap(s, (l, r) => channelNonEmpty(l, r))

  /** Check if two services interfere with each other
    * @group interfere_predicate
    * @param l
    *   the left service
    * @param r
    *   the right service
    * @return
    *   true if they interfere
    */
  final def finalInterfereWith(l: Service, r: Service): Boolean = {
    antiReflexive(l, r) && (
      symmetric[Service](interfereWith)(l, r) ||
        (l.hardwareOwner.nonEmpty &&
          r.hardwareOwner.nonEmpty &&
          l.hardwareOwner.exists(ol =>
            r.hardwareOwner.exists(or => finalInterfereWith(ol, or))
          ) // service owner are exclusive
        )
    )
  }

  /** Check if two services are equivalent
    * @group equivalence_predicate
    * @param l
    *   the left service
    * @param r
    *   the right service
    * @return
    *   true if the services are equivalent
    */
  protected def areEquivalent(l: Service, r: Service): Boolean

  /** Check whether two transaction will not occur simultaneously
    * @group exclusive_predicate
    * @param l
    *   the left transaction
    * @param r
    *   the right transaction
    * @return
    *   true if they cannot occur simultaneously
    */
  protected def exclusiveWith(
      l: AtomicTransactionId,
      r: AtomicTransactionId
  ): Boolean

  /** Check if two services interfere with each other
    * @group interfere_predicate
    * @param l
    *   the left service
    * @param r
    *   the right service
    * @return
    *   true if they interfere
    */
  protected def interfereWith(l: Service, r: Service): Boolean

  /** Check whether two hardware cannot work simultaneously
    * @group interfere_predicate
    * @param l
    *   the left hardware
    * @param r
    *   the right hardware
    * @return
    *   true if they cannot work simultaneously
    */
  protected def interfereWith(l: Hardware, r: Hardware): Boolean

  protected def relationToMap[T](
      all: => Set[T],
      r: (T, T) => Boolean
  ): Map[T, Set[T]] =
    all.groupMapReduce(t => t)(t => all.filter(r(t, _)))(_ ++ _)

  private def reflexive[T](l: T, r: T): Boolean = l == r

  private def symmetric[T](relation: (T, T) => Boolean)(l: T, r: T): Boolean =
    relation(l, r) || relation(r, l)

  private def antiReflexive[T](l: T, r: T): Boolean = l != r

  private def addToClosure[T](
      relation: (T, T) => Boolean
  )(s: T, closures: Set[Set[T]]): Set[Set[T]] =
    if (closures.exists(_.exists(cs => relation(s, cs)))) {
      closures
        .map(c => if (c.exists(cs => relation(s, cs))) c + s else c)
        .foldLeft(Set.empty[Set[T]])((acc, c) => {
          if (acc.exists(c2 => c.intersect(c2).nonEmpty))
            acc.map(c2 => if (c.intersect(c2).nonEmpty) c2 ++ c else c2)
          else
            acc + c
        })
    } else
      closures + Set(s)

  private def equivalenceClasses[T](
      relation: (T, T) => Boolean
  )(in: Set[T]): Set[Set[T]] = {
    val closure = addToClosure[T](relation) _
    in.foldLeft(Set.empty[Set[T]])((acc, s) => closure(s, acc))
  }

  @deprecated(
    "Poor performance implementation, consider using MONOSAT problem to identify channel"
  )
  final def channel(
      t: AtomicTransactionId,
      in: Set[AtomicTransactionId]
  ): Set[Service] =
    (for {
      ts <- purifiedTransactions.get(t)
      ss = ts.toSet
    } yield {
      in
        .flatMap(purifiedTransactions.get)
        .flatMap(tp => tp.toSet.intersect(ss))
    }) getOrElse Set.empty

  @deprecated(
    "Poor performance implementation, consider using recursive transaction equivalence classes splitting perhaps implemented within MONOSAT"
  )
  final def equivalentTransactionSets(
      s: Set[AtomicTransactionId],
      in: Set[AtomicTransactionId]
  ): Set[Set[AtomicTransactionId]] = {
    val transactionsClasses = equivalenceTransactionClasses(in)
      .flatMap(c => c.intersect(s).map(_ -> c))
      .toMap
    s.foldLeft(
      (
        Set.empty[AtomicTransactionId],
        Set.empty[Set[AtomicTransactionId]]
      )
    )((acc, t) => {
      val (current, result) = acc
      val iniChannel = channel(t, current)
      if (current.isEmpty) {
        (current + t, (transactionsClasses(t) - t).map(Set(_)))
      } else {
        val r = (
          current + t,
          result.flatMap(st =>
            (transactionsClasses(t) -- st - t)
              .filter(tp =>
                channel(tp, st) == iniChannel && st
                  .forall(!finalExclusive(_, tp))
              )
              .map(st + _)
          )
        )
        r
      }
    })._2
  }
}

object InterferenceSpecification {
  type Path[A] = List[A]
  type AtomicTransaction = Path[Service]
  type PhysicalScenario = Set[AtomicTransactionId]
  type Channel = Set[Service]

  trait Id extends Ordered[Id] {
    val id: Symbol

    override def toString: String = id.name

    def compare(that: Id): Int = id.name.compare(that.id.name)
  }

  final case class AtomicTransactionId(id: Symbol) extends Id

  final case class PhysicalScenarioId(id: Symbol) extends Id

  final case class PhysicalMultiTransactionId(id: Symbol) extends Id

  final case class ChannelId(id: Symbol) extends Id

  def multiTransactionId(
      t: Iterable[PhysicalScenarioId]
  ): PhysicalMultiTransactionId =
    PhysicalMultiTransactionId(
      Symbol(t.map(_.id.name).toArray.sorted.mkString("< ", " || ", " >"))
    )

  def channelId(t: Set[Service]): ChannelId =
    ChannelId(
      Symbol(t.map(_.toString).toArray.sorted.mkString("{ ", ", ", " }"))
    )

  def groupedScenarioLitId(
      s: Set[PhysicalScenarioId]
  ): PhysicalMultiTransactionId =
    PhysicalMultiTransactionId(
      Symbol(multiTransactionId(s).id.name.replace(" ", ""))
    )

  trait Default extends InterferenceSpecification {
    self: Platform =>

    def interfereWith(l: Service, r: Service): Boolean =
      l.hardwareOwner == r.hardwareOwner

    def areEquivalent(l: Service, r: Service): Boolean = false

    def interfereWith(l: Hardware, r: Hardware): Boolean = false

    def isTransparentTransaction(t: AtomicTransactionId): Boolean =
      false

    def transactionInterfereWith(t: AtomicTransactionId): Set[Service] =
      Set.empty

    def transactionNotInterfereWith(
        t: AtomicTransactionId
    ): Set[Service] =
      Set.empty

    protected def exclusiveWith(
        l: AtomicTransactionId,
        r: AtomicTransactionId
    ): Boolean =
      l.usedInitiators == r.usedInitiators
  }
}
