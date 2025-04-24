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

package onera.pmlanalyzer.pml.operators

import onera.pmlanalyzer.pml.model.configuration.TransactionLibrary.{
  UserScenarioId,
  UserTransactionId
}
import onera.pmlanalyzer.pml.model.service.Service
import onera.pmlanalyzer.views.interference.model.specification.InterferenceSpecification.{
  PhysicalScenarioId,
  PhysicalTransactionId
}
import onera.pmlanalyzer.pml.model.relations.DemandRelation
import onera.pmlanalyzer.pml.model.relations.CapacityRelation
import onera.pmlanalyzer.views.interference.model.specification.InterferenceSpecification.*
import onera.pmlanalyzer.views.interference.operators.Transform
import sourcecode.{File, Line}

private[operators] trait Demand[L, R] {
  def apply(l: L, r: R)(using line: Line, file: File): Unit
}

object Demand {

  /** If an element l of type T can be exclusive with another element r of type
    * T, the following operator can be used {{{l exclusiveWith r}}}
    */
  trait Ops {

    extension [L](l: L) {
      def hasDemand[R](
          r: R
      )(using d: Demand[L, R], line: Line, file: File): Unit =
        d(l, r)
    }
  }

  given [LUT <: UserTransactionId, R](using
      d: Demand[PhysicalTransactionId, R],
      transform: Transform[UserTransactionId, Option[PhysicalTransactionId]]
  ): Demand[LUT, R] with {
    def apply(l: LUT, r: R)(using line: Line, file: File): Unit =
      transform(l) match {
        case Some(x) => x hasDemand r
        case None    =>
      }
  }

  given [LUS <: UserScenarioId, R](using
      d: Demand[PhysicalTransactionId, R],
      transform: Transform[UserScenarioId, Set[PhysicalTransactionId]]
  ): Demand[LUS, R] with {
    def apply(l: LUS, r: R)(using line: Line, file: File): Unit =
      for {
        x <- transform(l)
      } yield x hasDemand r
  }

  given [LPS <: PhysicalScenarioId, R](using
     d: Demand[PhysicalTransactionId, R],
     transform: Transform[PhysicalScenarioId, Set[PhysicalTransactionId]]
  ): Demand[LPS, R] with {
    def apply(l: LPS, r: R)(using line: Line, file: File): Unit =
      for {
        t <- transform(l)
      } yield t hasDemand r
  }

  /**
   * We can generate a proof that a demand of type R is assignable to a type L
   * If we can find a relation containing super types of L and R
   */
  given [CL, CR, L <: CL, R <: CR](using dr: DemandRelation[CL, CR]): Demand[
    L,
    R
  ] with {
    def apply(l: L, r: R)(using line: Line, file: File): Unit = dr.add(l, r)
  }
}
