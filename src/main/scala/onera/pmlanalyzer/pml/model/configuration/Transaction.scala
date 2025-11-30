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

import onera.pmlanalyzer.pml.model.configuration.TransactionLibrary.UserTransactionId
import onera.pmlanalyzer.pml.model.service.Service
import onera.pmlanalyzer.pml.model.software.Application
import onera.pmlanalyzer.pml.model.utils.ReflexiveInfo
import onera.pmlanalyzer.pml.model.{PMLNode, PMLNodeBuilder, PMLNodeMap}
import onera.pmlanalyzer.*
import onera.pmlanalyzer.pml.operators.DelayedTransform
import onera.pmlanalyzer.pml.operators.DelayedTransform.TransactionParam
import sourcecode.Name

/** Class encoding the defined transactions (not already used)
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
private[pmlanalyzer] final class Transaction private (
    val userName: UserTransactionId,
    val iniTgt: () => Set[(Service, Service)],
    val sw: () => Set[Application],
    info: ReflexiveInfo
) extends PMLNode(info) {

  /** Name of the node
   *
   * @group identifier
   */
  override val name: Symbol = userName.id

  /** Consider the transaction for the analysis
      *
      * @return
      *   the used transaction class
      */
  def used(using
      givenInfo: ReflexiveInfo,
      map: PMLNodeMap[UsedTransaction]
  ): UsedTransaction =
    UsedTransaction(userName, iniTgt(), sw())
}

/** Builder of platform [[Transaction]]
 *
 * @group transaction_class
    */
private[pmlanalyzer] object Transaction extends PMLNodeBuilder[Transaction] {

  /** A transaction can be built from an application targeting a load or a
   * store service
   *
   * @param iniTgt
   * the application/target service used
   * @param name
   * the implicit name of the transaction (deduced from val used during
   * instantiation)
   * @tparam A
   * the type of requests
   * @return
   * the transaction (not used for now)
   */
  def apply[A](
      iniTgt: => A
  )(using
      name: Name,
      givenInfo: ReflexiveInfo,
      map: PMLNodeMap[Transaction],
      ev: DelayedTransform[A, TransactionParam]
  ): Transaction = {
    val result = iniTgt.toTransactionParam
    apply(UserTransactionId(Symbol(name.value)), result._1, result._2)
  }

  /** A transaction can be from an application targeting a load or a store
   * service
   *
   * @param name
   * explicit name of the transaction
   * @param iniTgt
   * the application/target service used
   * @tparam A
   * the type of requests
   * @return
   * the transaction (not used for now)
   */
  def apply[A](name: String, iniTgt: => A)(using
      givenInfo: ReflexiveInfo,
      map: PMLNodeMap[Transaction],
      ev: DelayedTransform[A, TransactionParam]
  ): Transaction = {
    val result = iniTgt.toTransactionParam
    apply(UserTransactionId(Symbol(name)), result._1, result._2)
  }

  /** Build a transaction from a bunch of transactions, this should not be used
      * with anonymous transaction
      *
      * @param tail
      *   the set of transactions
      * @param name
      *   the implicit name of the transaction (same as the variable used to refer
      *   to it)
      * @return
      *   a transaction
      */
  def apply(
      head: Transaction,
      next: Transaction,
      tail: Transaction*
  )(using
      name: Name,
      givenInfo: ReflexiveInfo,
      map: PMLNodeMap[Transaction]
  ): Transaction =
    apply(name.value, head, next, tail: _*)

  def apply(
      name: String,
      head: Transaction,
      next: Transaction,
      tail: Transaction*
  )(using
      givenInfo: ReflexiveInfo,
      map: PMLNodeMap[Transaction]
  ): Transaction =
    apply(
      UserTransactionId(Symbol(name)),
      () => {
        (head +: next +: tail).flatMap(_.iniTgt()).toSet
      },
      () => {
        (head +: next +: tail).flatMap(_.sw()).toSet
      }
    )

  /** Main constructor of a transaction, note that transactions are memoized, so if
      * the same name is used in the same platform the constructor will send
      * back the previous definition of the transaction
      *
      * @param name
      *   the name of the transaction
      * @param iniTgt
      *   the set of initial/target services defining the transaction
      * @param sw
      *   the applications that may invoke this transaction
      * @return
      *   the transaction (not used for now)
      */
  def apply(
      name: UserTransactionId,
      iniTgt: () => Set[(Service, Service)],
      sw: () => Set[Application]
  )(using
      givenInfo: ReflexiveInfo,
      map: PMLNodeMap[Transaction]
  ): Transaction = {
    getOrElseUpdate(
      PMLNodeBuilder.formatName(name.id, givenInfo.owner),
      new Transaction(name, iniTgt, sw, givenInfo)
    )
  }
}
