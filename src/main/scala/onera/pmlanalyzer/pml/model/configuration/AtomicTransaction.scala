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

package onera.pmlanalyzer.pml.model.configuration

import onera.pmlanalyzer.pml.model.{PMLNodeBuilder, PMLNodeMap}
import onera.pmlanalyzer.pml.model.configuration.TransactionLibrary.UserTransactionId
import onera.pmlanalyzer.pml.model.service.Service
import onera.pmlanalyzer.pml.model.software.Application
import onera.pmlanalyzer.pml.model.utils.ReflexiveInfo
import onera.pmlanalyzer.pml.operators.{ToTransaction, TransactionParam}
import sourcecode.Name

/** Class encoding the used defined transactions (not already used)
 *
 * @group transaction_class
    * @param userName
    *   the name of the node
    * @param iniTgt
    *   a by-name value providing the origin-destination service couples of the
    *   transaction (not evaluated during the object initialisation)
    * @param sw
    *   the application that can use this transaction
    */
final class AtomicTransaction private (
    val userName: UserTransactionId,
    val iniTgt: () => Set[(Service, Service)],
    val sw: () => Set[Application],
    info: ReflexiveInfo
) extends ScenarioLike(userName.id, info) {

  /** Consider the transaction for the analysis
      *
      * @return
      *   the used transaction
      */
  def used(using
      givenInfo: ReflexiveInfo,
      map: PMLNodeMap[UsedTransaction]
  ): UsedTransaction =
    UsedTransaction(userName, iniTgt(), sw())

  override def toString: String = s"$userName"
}

/** Builder of platform [[AtomicTransaction]]
 *
    * @group transaction_class
    */
object AtomicTransaction extends PMLNodeBuilder[AtomicTransaction] {

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
  def apply[A: ToTransaction](
      iniTgt: => A
  )(using
      name: Name,
      givenInfo: ReflexiveInfo,
      map: PMLNodeMap[AtomicTransaction]
  ): AtomicTransaction = {
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
  )(using
      givenInfo: ReflexiveInfo,
      map: PMLNodeMap[AtomicTransaction]
  ): AtomicTransaction = {
    getOrElseUpdate(
      PMLNodeBuilder.formatName(name.id, givenInfo.owner),
      new AtomicTransaction(name, iniTgt, sw, givenInfo)
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
  def apply[A: ToTransaction](name: String, iniTgt: => A)(using
      givenInfo: ReflexiveInfo,
      map: PMLNodeMap[AtomicTransaction]
  ): AtomicTransaction = {
    val result = TransactionParam(iniTgt)
    apply(UserTransactionId(Symbol(name)), result._1, result._2)
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
      from: AtomicTransaction
  )(using
      name: Name,
      info: ReflexiveInfo,
      map: PMLNodeMap[AtomicTransaction]
  ): AtomicTransaction =
    apply(UserTransactionId(Symbol(name.value)), from.iniTgt, from.sw)

  /** A transaction can be build from another transaction
   *
   * @param name
   *   explicit name of the transaction
   * @param from
   *   the initial transaction
   * @return
   *   the transaction (not used for now)
   */
  def apply(
      name: String,
      from: AtomicTransaction
  )(using
      info: ReflexiveInfo,
      map: PMLNodeMap[AtomicTransaction]
  ): AtomicTransaction =
    apply(UserTransactionId(Symbol(name)), from.iniTgt, from.sw)
}
