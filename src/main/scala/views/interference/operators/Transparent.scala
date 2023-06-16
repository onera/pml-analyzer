package views.interference.operators

import views.interference.model.relations.TransparentSet
import views.interference.model.specification.InterferenceSpecification.PhysicalTransactionId

private[operators] trait Transparent[T] {
  def apply(x:T):Unit
}

object Transparent{

  /**
    * If an element x of type T is transparent then the operator can be used as follows
    * {{{x.isTransparent}}}
    */
  trait Ops {
    extension[T](x:T){
      /**
        * The element x is discarded for interference analysis
        * @param ev proof that T can be discarded
        */
      def isTransparent(using ev:Transparent[T]): Unit = ev(x)
    }
  }

  given [T](using ev:TransparentSet[T]):Transparent[T] with {
    def apply(x:T): Unit = ev.value += x
  }

  given [U] (using transformation: Transform[U,Option[PhysicalTransactionId]],
             ev:Transparent[PhysicalTransactionId]): Transparent[U] with {
    def apply(x:U): Unit = for {id <- transformation(x)} ev(id)
  }


  given [V] (using transformation: Transform[V,Set[PhysicalTransactionId]],
             ev:Transparent[PhysicalTransactionId]): Transparent[V] with {
    def apply(x: V): Unit = for {id <- transformation(x)} ev(id)
  }

}
