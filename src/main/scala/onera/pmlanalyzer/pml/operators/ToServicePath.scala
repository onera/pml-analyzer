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

package onera.pmlanalyzer.pml.operators

import onera.pmlanalyzer.pml.operators.*
import onera.pmlanalyzer.pml.model.hardware.*
import onera.pmlanalyzer.pml.model.service.*
import onera.pmlanalyzer.views.interference.model.specification.InterferenceSpecification.{
  PhysicalTransaction,
  PhysicalTransactionId
}

trait ToServicePath[T] {
  def apply(x: T): Set[PhysicalTransaction]
}

object ToServicePath {

  trait Ops {
    extension [T](x: T) {

      /** Check if the transaction contains only one service
       *
       * @return
       * true if only one service
       */
      def noSingletonPaths(using ev: ToServicePath[T]): Boolean =
        paths.forall(_.size > 1)

      /** Method that should be provided by sub-classes to access to the path
       *
       * @return
       * the set of service paths
       */
      def paths(using ev: ToServicePath[T]): Set[PhysicalTransaction] = ev(x)

      /** Check if the target is in the possible targets of the transaction
       *
       * @param t
       * target to find
       * @return
       * true if the target is contained
       */
      def pathTargetIs(
          t: Target
      )(using ev: ToServicePath[T], p: Provided[Target, Service]): Boolean =
        pathTargets.contains(t)

      /** Provide the targets of the transaction
       *
       * @return
       * the set of targets
       */
      def pathTargets(using
          ev: ToServicePath[T],
          p: Provided[Target, Service]
      ): Set[Target] =
        paths.filter(_.size >= 2).flatMap(t => t.last.targetOwner)

      /** Provide the initiators fo a transaction
       *
       * @return
       * the set of initiators
       */
      def pathInitiators(using
          ev: ToServicePath[T],
          p: Provided[Initiator, Service]
      ): Set[Initiator] =
        paths.filter(_.nonEmpty).flatMap(t => t.head.initiatorOwner)

      /** Check is the initiator is in the possible initiators of the transaction
       *
       * @param ini
       * initiator to find
       * @return
       * true if the initiator is contained
       */
      def pathInitiatorIs(
          ini: Initiator
      )(using ev: ToServicePath[T], p: Provided[Initiator, Service]): Boolean =
        pathInitiators.contains(ini)

      /** Check if the transaction is a load transaction
       *
       * @return
       * true if target services are loads
       */
      def isLoad(using ev: ToServicePath[T]): Boolean =
        paths.forall(path => path.nonEmpty && path.head.isInstanceOf[Load])

      /** Check if the transaction is a store transaction
       *
       * @return
       * true if target services are stores
       */
      def isStore(using ev: ToServicePath[T]): Boolean =
        paths.forall(path => path.nonEmpty && path.head.isInstanceOf[Store])
    }
  }
}
