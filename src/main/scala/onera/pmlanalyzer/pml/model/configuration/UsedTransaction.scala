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

import onera.pmlanalyzer.pml.model.{PMLNode, PMLNodeBuilder, PMLNodeMap}
import onera.pmlanalyzer.pml.model.configuration.TransactionLibrary.{
  UserScenarioId,
  UserTransactionId
}
import onera.pmlanalyzer.pml.model.service.Service
import onera.pmlanalyzer.pml.model.software.Application
import onera.pmlanalyzer.pml.model.utils.{Message, ReflexiveInfo}
import onera.pmlanalyzer.views.interference.model.specification.InterferenceSpecification.{
  PhysicalTransaction,
  PhysicalTransactionId
}

/** Class encoding the user defined transactions used in the configuration
 *
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
    info: ReflexiveInfo
) extends PMLNode(info) {

  val name: Symbol = userName.id

  /** Try to find a physical transaction to the user transaction
      *
      * @return
      *   the physical transaction if possible
      */
  def toPhysical(
      transactionsByName: Map[PhysicalTransactionId, PhysicalTransaction]
  )(using
      r: ReflexiveInfo,
      map: PMLNodeMap[Scenario],
      mapU: PMLNodeMap[UsedScenario]
  ): Option[PhysicalTransactionId] = transactionsByName
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
  )(using
      givenInfo: ReflexiveInfo,
      map: PMLNodeMap[UsedTransaction]
  ): UsedTransaction = {
    getOrElseUpdate(
      PMLNodeBuilder.formatName(name.id, givenInfo.owner),
      new UsedTransaction(name, iniTgt, sw, givenInfo)
    )
  }

}
