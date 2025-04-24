/** *****************************************************************************
  *
  *
  *

package onera.pmlanalyzer.pml.operators

import onera.pmlanalyzer.pml.operators.*
import onera.pmlanalyzer.pml.model.service.Service
import onera.pmlanalyzer.pml.model.hardware.Hardware
import onera.pmlanalyzer.views.interference.model.specification.InterferenceSpecification.PhysicalTransactionId
import onera.pmlanalyzer.pml.model.relations.{
  DemandRelation,
  CapacityRelation,
  ProvideRelation
}
import sourcecode.{File, Line}

private[operators] trait Capacity[L, R] {
  def apply(l: L, r: R)(using line: Line, file: File): Unit
}

object Capacity {

  /** If an element l of type T can be exclusive with another element r of type
   * T, the following operator can be used {{{l exclusiveWith r}}}
   */
  trait Ops {
    extension [L](l: L) {
      def hasCapacity[R](
          r: R
      )(using d: Capacity[L, R], line: Line, file: File): Unit =
        d(l, r)
    }
  }

  given [LH <: Hardware, R](using
      c: Capacity[Service, R],
      pr: Provided[LH, Service]
  ): Capacity[LH, R] with {
    def apply(l: LH, r: R)(using line: Line, file: File): Unit =
      for {
        s <- l.services
      } yield s hasCapacity r
  }

  /**
   * We can generate a proof that a capacity of type R is assignable to a type L
   * If we can find a relation containing syper types of L and R
   */
  given [CL, CR, L <: CL, R <: CR](using
      cr: CapacityRelation[CL, CR]
  ): Capacity[L, R] with {
    def apply(l: L, r: R)(using line: Line, file: File): Unit = cr.add(l, r)
  }
}
