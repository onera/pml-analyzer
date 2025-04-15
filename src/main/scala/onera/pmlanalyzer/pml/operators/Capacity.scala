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

package onera.pmlanalyzer.pml.model.operators

import onera.pmlanalyzer.pml.model.service.Service
import onera.pmlanalyzer.views.interference.model.specification.InterferenceSpecification.PhysicalTransactionId
import onera.pmlanalyzer.pml.model.relations.DemandRelation
import onera.pmlanalyzer.pml.model.relations.CapacityRelation

private[operators] trait Capacity[T] {
  def apply(l: T, r: T): Unit
}

object Capacity {

  /** If an element l of type T can be exclusive with another element r of type
    * T, the following operator can be used {{{l exclusiveWith r}}}
    */
  trait Ops {

    extension (t: PhysicalTransactionId) {
      def hasDemand(d: Int)(using dr: DemandRelation): Unit =
        // dr.hasDemand(t, d)
        dr.demandOfTransaction += (t -> d)
    }

    extension (s: Service) {
      def hasCapacity(c: Int)(using cr: CapacityRelation): Unit =
        // cr.hasCapacity(s, c)
        cr.capacityOfService += (s -> c)
    }

  }

  // given [PhysicalTransactionId, Int](using
  //     dr: DemandRelation
  // ): DemandRelation with {
  //   def hasDemand(t: PhysicalTransactionId, d: Int): Unit =
  //     dr.demandOfTransaction += (t -> d)
  // }

  // given [LS <: Service, Int](using
  //     cr: CapacityRelation
  // ): CapacityRelation with {
  //   def hasCapacity(s: PhysicalTransactionId, c: Int): Unit =
  //     cr.demandOfTransaction += (s -> c)
  // }

}
