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

package onera.pmlanalyzer.views.interference.model.specification

import onera.pmlanalyzer.pml.exporters.FileManager
import onera.pmlanalyzer.pml.model.configuration.TransactionLibrary
import onera.pmlanalyzer.pml.model.hardware.{Hardware, Platform}
import onera.pmlanalyzer.pml.model.service.Service
import onera.pmlanalyzer.pml.model.software.Application
import onera.pmlanalyzer.pml.operators.*
import onera.pmlanalyzer.views.interference.model.relations.*
import onera.pmlanalyzer.views.interference.model.specification.InterferenceSpecification.PhysicalTransactionId

import java.io.FileWriter
import scala.collection.mutable.Set as MSet

/** Trait providing a wide range of modelling features to specify assumption
  */
trait TableBasedInterferenceSpecification
    extends InterferenceSpecification
    with InterfereRelation.Instances
    with NotInterfereRelation.Instances
    with ExclusiveRelation.GeneralInstances
    with ExclusiveRelation.ApplicationInstances
    with EquivalenceRelation.Instances
    with TransparentSet.Instances {
  self: Platform =>

  /** The set of services provided by the platform
    * @group service_relation
    */
  final lazy val services: Set[Service] =
    implicitly[Provided[Platform, Service]].apply(self)

  /** Derive implementation from [[physicalTransactionIdInterfereWithService]]
    * @param t
    *   the identifier of the transaction
    * @return
    *   a set of services
    */
  final def transactionInterfereWith(t: PhysicalTransactionId): Set[Service] =
    physicalTransactionIdInterfereWithService.get(t).getOrElse(Set.empty)

  /** Derive implementation from
    * [[physicalTransactionIdNotInterfereWithService]]
    * @param t
    *   the identifier of the transaction
    * @return
    *   a set of services
    */
  final def transactionNotInterfereWith(
      t: PhysicalTransactionId
  ): Set[Service] =
    physicalTransactionIdNotInterfereWithService.get(t).getOrElse(Set.empty)

  /** Derive implementation from [[transactionIsTransparent]]
    * @param t
    *   the identifier of the transaction
    * @return
    *   true is the transaction is discarded
    */
  final def isTransparentTransaction(t: PhysicalTransactionId): Boolean =
    transactionIsTransparent.value.contains(t)

  /** Derive implementation from [[serviceInterfere]]
   *
    * @param l
    *   the left service
    * @param r
    *   the right service
    * @return
    *   true if they interfere
    */
  def interfereWith(l: Service, r: Service): Boolean =
    serviceInterfere
      .get(l)
      .getOrElse(Set.empty[Service])
      .contains(r)

  /** Derive implementation from [[hardwareInterfere]]
   *
    * @param l
    *   the left hardware
    * @param r
    *   the right hardware
    * @return
    *   true if they cannot work simultaneously
    */
  final def interfereWith(l: Hardware, r: Hardware): Boolean =
    hardwareInterfere
      .get(l)
      .getOrElse(Set.empty[Hardware])
      .contains(r)

  /** Derive implementation from [[serviceEquivalent]]
    * @param l
    *   the left service
    * @param r
    *   the right service
    * @return
    *   true if the services are equivalent
    */
  final def areEquivalent(l: Service, r: Service): Boolean =
    serviceEquivalent
      .get(l)
      .getOrElse(Set.empty[Service])
      .contains(r)

  /** Derive implementation from [[transactionExclusive]], [[swExclusive]] or
    * same hardware owner
    * @param l
    *   the left transaction
    * @param r
    *   the right transaction
    * @return
    *   true if they cannot occur simultaneously
    */
  final def exclusiveWith(
      l: PhysicalTransactionId,
      r: PhysicalTransactionId
  ): Boolean = {
    val tExclusive = transactionExclusive
      .get(l)
      .getOrElse(Set.empty[PhysicalTransactionId])
      .contains(r)
    val samePLUsed = l.initiator.intersect(r.initiator).nonEmpty
    val differentAppUsed = Application.all
      .subsets(2)
      .filter(s => {
        val (al, ar) = (s.head, s.last)
        val lUsed = s.filter(sw => transactionsBySW(sw).contains(l))
        val rUsed = s.filter(sw => transactionsBySW(sw).contains(r))
        lUsed.contains(al) && rUsed.contains(ar) || lUsed.contains(ar) && rUsed
          .contains(al)
      })
    val appExclusive =
      differentAppUsed.nonEmpty && differentAppUsed.forall(s => {
        val (al, ar) = (s.head, s.last)
        swExclusive.get(al).getOrElse(MSet.empty[Application]).contains(ar)
      })
    samePLUsed || tExclusive || appExclusive
  }
}

object TableBasedInterferenceSpecification {

  /** Default implementation of [[TableBasedInterferenceSpecification]]
    */
  trait Default extends TableBasedInterferenceSpecification {
    self: Platform with TransactionLibrary =>

    final override def interfereWith(l: Service, r: Service): Boolean =
      l.hardwareOwner == r.hardwareOwner || super.interfereWith(l, r)

  }
}
