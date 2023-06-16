package pml.operators

import pml.model.service.*
import pml.operators.*
import pml.model.software.Application
import AsTransaction.TransactionParam
import pml.model.hardware.Initiator

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
