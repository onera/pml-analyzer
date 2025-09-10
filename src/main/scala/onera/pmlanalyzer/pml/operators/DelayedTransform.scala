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

import onera.pmlanalyzer.pml.model.configuration.Scenario
import onera.pmlanalyzer.pml.model.hardware.Initiator
import onera.pmlanalyzer.pml.model.service.{Load, Service, Store}
import onera.pmlanalyzer.pml.model.software.Application

trait DelayedTransform[L, R] {
  /**
   * By-name implementation of transform
   * Note that it is MANDATORY for Scenario definition since
   * its iniTgt and sw parameters should be evaluated only when triggering used keyword
   * @param l by-name value L to transform into R
   * @return the resulting R
   */
  def apply(l: => L): R
}

object DelayedTransform {
  type TransactionParam =
    (() => Set[(Service, Service)], () => Set[Application])

  given DelayedTransform[Scenario, TransactionParam] with {
    def apply(a: => Scenario): TransactionParam =
      (a.iniTgt, a.sw)
  }

  /** Utility function to convert an a set of application/target service to the
   * set of initial/target services
   *
   * @return
   * the set of initial/target services and of applications invoking them
   */

  given applicationUsed[T <: Load | Store](using
                                           u: Used[Application, Initiator],
                                           p: Provided[Initiator, T]
                                          ): DelayedTransform[Set[(Application, T)], TransactionParam] with {
    def apply(a: => Set[(Application, T)]): TransactionParam = (
      () => {
        a.flatMap(as =>
          as._1.hostingInitiators.flatMap(_.provided[T]).map(_ -> as._2)
        )
      },
      () => a.map(_._1)
    )
  }

  given initiatorUsed[T <: Load | Store](using
                                         p: Provided[Initiator, T]
                                        ): DelayedTransform[Set[(Initiator, T)], TransactionParam] with {
    def apply(a: => Set[(Initiator, T)]): TransactionParam =
      (
        () => {
          a.flatMap(as => as._1.provided[T].map(_ -> as._2))
        },
        () => Set.empty
      )
  }
}
