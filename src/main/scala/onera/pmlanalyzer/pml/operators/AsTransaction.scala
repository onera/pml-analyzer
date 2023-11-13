/*******************************************************************************
 * Copyright (c)  2021. ONERA
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

import onera.pmlanalyzer.pml.model.service.*
import onera.pmlanalyzer.pml.operators.*
import onera.pmlanalyzer.pml.model.software.Application
import AsTransaction.TransactionParam
import onera.pmlanalyzer.pml.model.hardware.Initiator

trait AsTransaction[A] {
  def apply(a: => A): TransactionParam
}

object AsTransaction{
  type TransactionParam = (() => Set[(Service, Service)], () => Set[Application])
  
  trait Ops {
    def TransactionParam[A](a: => A)(using ev:AsTransaction[A]): TransactionParam = ev(a)
  }

  /**
   * Utility function to convert an a set of application/target service to the set of initial/target services
   * @return the set of initial/target services and of applications invoking them
   */

  given applicationUsed[T<: Load | Store] (using u:Used[Application,Initiator], p:Provided[Initiator,T]) : AsTransaction[Set[(Application,T)]] with {
    def apply(a: => Set[(Application, T)]): (() => Set[(Service, Service)], () => Set[Application]) = (
      () => {
        a.flatMap(as => as._1.hostingInitiators.flatMap(_.provided[T]).map(_ -> as._2))
      },
      () => a.map(_._1))
  }

  given initiatorUsed[T<: Load | Store] (using p: Provided[Initiator, T]): AsTransaction[Set[(Initiator, T)]] with {
    def apply(a: => Set[(Initiator, T)]): (() => Set[(Service, Service)], () => Set[Application]) =
      (() => {
        a.flatMap(as => as._1.provided[T].map(_ -> as._2))
      },
        () => Set.empty)
  }

}
