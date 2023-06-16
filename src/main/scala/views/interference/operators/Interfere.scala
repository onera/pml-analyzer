package views.interference.operators

import pml.model.service.Service
import views.interference.model.relations.{InterfereRelation, NotInterfereRelation}
import views.interference.model.specification.InterferenceSpecification.PhysicalTransactionId

private[operators] trait Interfere[L, R] {
  def interfereWith(l: L, r: R): Unit

  def notInterfereWith(l: L, r: R): Unit
}

object Interfere {

  /**
    * If an element x of type L and an element y of type R can interfere then the operator can be used as follows:
    *
    * x interferes with y
    * {{{x interfereWith y}}}
    * x does not interfere with y
    * {{{x notInterfereWith y}}}
    */
  trait Ops {

    extension[L] (self: L) {
      def interfereWith[R](that: R)(using ev: Interfere[L, R]): Unit = ev.interfereWith(self, that)
      def interfereWith[R](that: Iterable[R])(using ev: Interfere[L, R]): Unit = for {x <- that} ev.interfereWith(self, x)
      def notInterfereWith[R](that: R)(using ev: Interfere[L, R]): Unit = ev.notInterfereWith(self, that)
      def notInterfereWith[R](that: Iterable[R])(using ev: Interfere[L, R]): Unit = for {x <- that} ev.notInterfereWith(self, x)
    }
  }

  given[L, R] (using a: InterfereRelation[L, R], n: NotInterfereRelation[L, R]): Interfere[L, R] with {
    def interfereWith(l: L, r: R): Unit = a.add(l, r)

    def notInterfereWith(l: L, r: R): Unit = n.add(l, r)
  }

  given[LS <: Service, RS <: Service] (using ev: Interfere[Service, Service]): Interfere[LS, RS] with {
    def interfereWith(l: LS, r: RS): Unit = ev.interfereWith(l, r)

    def notInterfereWith(l: LS, r: RS): Unit = ev.notInterfereWith(l, r)
  }

  given[R <: Service] (using ev: Interfere[PhysicalTransactionId, Service]): Interfere[PhysicalTransactionId, R] with {
    def interfereWith(l: PhysicalTransactionId, r: R): Unit = ev.interfereWith(l, r)

    def notInterfereWith(l: PhysicalTransactionId, r: R): Unit = ev.notInterfereWith(l, r)
  }

  given[L, RS <: Service] (using
                           transformation: Transform[L, Set[PhysicalTransactionId]],
                           ev: Interfere[PhysicalTransactionId, Service]): Interfere[L, RS] with {
    def interfereWith(l: L, r: RS): Unit =
      for {id <- transformation(l)}
        ev.interfereWith(id, r)

    def notInterfereWith(l: L, r: RS): Unit =
      for {id <- transformation(l)}
        ev.notInterfereWith(id, r)
  }

  given[LP, RS <: Service] (using
                            transformation: Transform[LP, Option[PhysicalTransactionId]],
                            ev: Interfere[PhysicalTransactionId, Service]): Interfere[LP, RS] with {
    def interfereWith(l: LP, r: RS): Unit =
      for {id <- transformation(l)}
        ev.interfereWith(id, r)

    def notInterfereWith(l: LP, r: RS): Unit =
      for {id <- transformation(l)}
        ev.notInterfereWith(id, r)
  }
}
